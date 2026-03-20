package com.library.app.service;

import com.library.app.domain.Book;
import com.library.app.dto.book.AdminBookUpsertRequest;
import com.library.app.dto.book.BookResponse;
import com.library.app.repository.BookRepository;
import com.library.app.repository.LoanRepository;
import com.library.app.web.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public Page<BookResponse> listBooks(String keyword, Pageable pageable) {
        Page<Book> books = (keyword == null || keyword.isBlank())
                ? bookRepository.findAll(pageable)
                : bookRepository.searchByKeyword(keyword.trim(), pageable);
        return books.map(this::toResponse);
    }

    public BookResponse getBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        return toResponse(book);
    }

    public BookResponse createBook(AdminBookUpsertRequest request) {
        validateCopies(request.totalCopies(), request.availableCopies());
        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .category(request.category())
                .description(request.description())
                .totalCopies(request.totalCopies())
                .availableCopies(request.availableCopies())
                .build();
        return toResponse(bookRepository.save(book));
    }

    public BookResponse updateBook(Long id, AdminBookUpsertRequest request) {
        validateCopies(request.totalCopies(), request.availableCopies());
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setCategory(request.category());
        book.setDescription(request.description());
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.availableCopies());
        return toResponse(bookRepository.save(book));
    }

    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        if (loanRepository.existsByBookIdAndReturnedAtIsNull(id)) {
            throw new ConflictException("BOOK_HAS_ACTIVE_LOANS", "Book has active loans");
        }
        bookRepository.delete(book);
    }

    private static void validateCopies(int totalCopies, int availableCopies) {
        if (availableCopies > totalCopies) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "availableCopies cannot exceed totalCopies");
        }
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getCategory(),
                book.getDescription(),
                book.getTotalCopies(),
                book.getAvailableCopies()
        );
    }
}