package com.library.app.repository;

import com.library.app.domain.Book;
import com.library.app.domain.Loan;
import com.library.app.domain.ReaderStatus;
import com.library.app.domain.Role;
import com.library.app.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private LoanRepository loanRepository;

	@Test
	void savesUserBookAndLoan() {
		User reader = userRepository.save(User.builder()
				.username("alice")
				.passwordHash("hash")
				.role(Role.READER)
				.readerStatus(ReaderStatus.APPROVED)
				.build());

		Book book = bookRepository.save(Book.builder()
				.title("Clean Code")
				.author("Robert C. Martin")
				.totalCopies(2)
				.availableCopies(1)
				.build());

		LocalDateTime now = LocalDateTime.now();
		Loan loan = loanRepository.save(Loan.builder()
				.reader(reader)
				.book(book)
				.borrowedAt(now)
				.dueAt(now.plusDays(14))
				.build());

		assertThat(userRepository.findByUsername("alice")).isPresent();
		assertThat(bookRepository.findById(book.getId())).isPresent();
		assertThat(loanRepository.findById(loan.getId())).isPresent();
		assertThat(loanRepository.findById(loan.getId()).orElseThrow().getReader().getUsername()).isEqualTo("alice");
	}
}
