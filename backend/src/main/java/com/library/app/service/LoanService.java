package com.library.app.service;

import com.library.app.config.LibraryProperties;
import com.library.app.domain.Book;
import com.library.app.domain.Loan;
import com.library.app.domain.ReaderStatus;
import com.library.app.domain.Role;
import com.library.app.domain.User;
import com.library.app.dto.AdminLoanResponse;
import com.library.app.dto.LoanResponse;
import com.library.app.repository.BookRepository;
import com.library.app.repository.LoanRepository;
import com.library.app.repository.UserRepository;
import com.library.app.web.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

	private final UserRepository userRepository;
	private final BookRepository bookRepository;
	private final LoanRepository loanRepository;
	private final LibraryProperties libraryProperties;

	@Transactional
	public LoanResponse borrow(Long bookId) {
		User reader = loadCurrentUser();
		if (reader.getRole() != Role.READER) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ONLY_READERS_MAY_BORROW");
		}
		if (reader.getReaderStatus() != ReaderStatus.APPROVED) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "READER_NOT_APPROVED");
		}

		Book book = bookRepository.findByIdForUpdate(bookId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND"));

		if (book.getAvailableCopies() <= 0) {
			throw new ConflictException("NO_AVAILABLE_COPIES", "No copies available");
		}
		if (loanRepository.existsByReader_IdAndBook_IdAndReturnedAtIsNull(reader.getId(), bookId)) {
			throw new ConflictException("DUPLICATE_ACTIVE_LOAN", "Already borrowing this book");
		}
		long active = loanRepository.countByReader_IdAndReturnedAtIsNull(reader.getId());
		if (active >= libraryProperties.getMaxActiveLoans()) {
			throw new ConflictException("MAX_ACTIVE_LOANS", "Active loan limit reached");
		}

		book.setAvailableCopies(book.getAvailableCopies() - 1);
		bookRepository.save(book);

		LocalDateTime now = LocalDateTime.now();
		Loan loan = loanRepository.save(Loan.builder()
				.reader(reader)
				.book(book)
				.borrowedAt(now)
				.dueAt(now.plusDays(libraryProperties.getLoanDays()))
				.build());

		return toResponse(loan);
	}

	@Transactional(readOnly = true)
	public List<LoanResponse> listMyLoans() {
		User user = loadCurrentUser();
		if (user.getRole() != Role.READER) {
			return List.of();
		}
		return loanRepository.findByReader_IdWithBookFetched(user.getId()).stream()
				.map(this::toResponse)
				.toList();
	}

	/**
	 * Reader returns their copy; second call is a no-op on inventory (idempotent).
	 */
	@Transactional
	public LoanResponse returnLoan(Long loanId) {
		User current = loadCurrentUser();
		if (current.getRole() != Role.READER) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ONLY_READERS_MAY_RETURN");
		}

		Loan loan = loanRepository.findByIdForReturn(loanId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "LOAN_NOT_FOUND"));

		if (!loan.getReader().getId().equals(current.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "NOT_YOUR_LOAN");
		}

		if (loan.getReturnedAt() != null) {
			return toResponse(loan);
		}

		LocalDateTime returnedAt = LocalDateTime.now();
		loan.setReturnedAt(returnedAt);

		Book book = bookRepository.findByIdForUpdate(loan.getBook().getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND"));
		book.setAvailableCopies(Math.min(book.getTotalCopies(), book.getAvailableCopies() + 1));

		loanRepository.save(loan);
		bookRepository.save(book);

		return toResponse(loan);
	}

	@Transactional(readOnly = true)
	public List<AdminLoanResponse> listAllLoansForAdmin() {
		return loanRepository.findAllWithReaderAndBookOrderByBorrowedAtDesc().stream()
				.map(this::toAdminResponse)
				.toList();
	}

	private User loadCurrentUser() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));
	}

	private LoanResponse toResponse(Loan loan) {
		Book book = loan.getBook();
		return new LoanResponse(
				loan.getId(),
				book.getId(),
				book.getTitle(),
				book.getAuthor(),
				loan.getBorrowedAt(),
				loan.getDueAt(),
				loan.getReturnedAt());
	}

	private AdminLoanResponse toAdminResponse(Loan loan) {
		Book book = loan.getBook();
		return new AdminLoanResponse(
				loan.getId(),
				loan.getReader().getUsername(),
				book.getId(),
				book.getTitle(),
				book.getAuthor(),
				loan.getBorrowedAt(),
				loan.getDueAt(),
				loan.getReturnedAt());
	}
}
