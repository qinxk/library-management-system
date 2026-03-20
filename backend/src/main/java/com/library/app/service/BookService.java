package com.library.app.service;

import com.library.app.domain.Book;
import com.library.app.dto.book.BookResponse;
import com.library.app.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

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