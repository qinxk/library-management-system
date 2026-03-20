package com.library.app.controller;

import com.library.app.domain.Book;
import com.library.app.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void anonymousCanListBooksWithKeywordAndPagination() throws Exception {
        bookRepository.save(Book.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .totalCopies(3)
                .availableCopies(3)
                .build());
        bookRepository.save(Book.builder()
                .title("Domain-Driven Design")
                .author("Eric Evans")
                .isbn("9780321125217")
                .totalCopies(2)
                .availableCopies(2)
                .build());

        mockMvc.perform(get("/api/books").param("keyword", "clean").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"));
    }

    @Test
    void anonymousCanGetBookDetail() throws Exception {
        Book book = bookRepository.save(Book.builder()
                .title("Refactoring")
                .author("Martin Fowler")
                .isbn("9780201485677")
                .totalCopies(1)
                .availableCopies(1)
                .build());

        mockMvc.perform(get("/api/books/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Refactoring"))
                .andExpect(jsonPath("$.author").value("Martin Fowler"));
    }

    @Test
    void getBookDetail_missingBook_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/books/{id}", 999999L))
                .andExpect(status().isNotFound());
    }
}