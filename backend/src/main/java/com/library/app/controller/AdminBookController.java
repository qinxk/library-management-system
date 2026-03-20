package com.library.app.controller;

import com.library.app.dto.book.AdminBookUpsertRequest;
import com.library.app.dto.book.BookResponse;
import com.library.app.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/books")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookController {

    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse create(@Valid @RequestBody AdminBookUpsertRequest request) {
        return bookService.createBook(request);
    }

    @PutMapping("/{id}")
    public BookResponse update(@PathVariable Long id, @Valid @RequestBody AdminBookUpsertRequest request) {
        return bookService.updateBook(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bookService.deleteBook(id);
    }
}