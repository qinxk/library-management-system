package com.library.app.controller;

import com.library.app.domain.Book;
import com.library.app.domain.ReaderStatus;
import com.library.app.domain.Role;
import com.library.app.domain.User;
import com.library.app.repository.BookRepository;
import com.library.app.repository.UserRepository;
import com.library.app.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminBookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void adminCanCreateBook() throws Exception {
        String adminToken = createTokenForRole("admin_x", Role.ADMIN);

        mockMvc.perform(post("/api/admin/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"The Pragmatic Programmer",
                                  "author":"Andrew Hunt",
                                  "isbn":"9780201616224",
                                  "category":"Programming",
                                  "description":"Classic",
                                  "totalCopies":5,
                                  "availableCopies":5
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("The Pragmatic Programmer"));
    }

    @Test
    void readerCannotCreateBook() throws Exception {
        String readerToken = createTokenForRole("reader_x", Role.READER);

        mockMvc.perform(post("/api/admin/books")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Test",
                                  "author":"A",
                                  "totalCopies":1,
                                  "availableCopies":1
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createBook_invalidCopies_returnsBadRequest() throws Exception {
        String adminToken = createTokenForRole("admin_y", Role.ADMIN);

        mockMvc.perform(post("/api/admin/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Bad",
                                  "author":"A",
                                  "totalCopies":1,
                                  "availableCopies":2
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteBook_withActiveLoan_returnsConflict() throws Exception {
        String adminToken = createTokenForRole("admin_z", Role.ADMIN);
        User reader = userRepository.save(User.builder()
                .username("reader_for_loan")
                .passwordHash(passwordEncoder.encode("pass123456"))
                .role(Role.READER)
                .readerStatus(ReaderStatus.APPROVED)
                .build());
        Book book = bookRepository.save(Book.builder()
                .title("Loaned")
                .author("Author")
                .totalCopies(1)
                .availableCopies(0)
                .build());

        persistActiveLoan(reader.getId(), book.getId());

        mockMvc.perform(delete("/api/admin/books/{id}", book.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BOOK_HAS_ACTIVE_LOANS"));
    }

    @Autowired
    private com.library.app.repository.LoanRepository loanRepository;

    private void persistActiveLoan(Long readerId, Long bookId) {
        User reader = userRepository.findById(readerId).orElseThrow();
        Book book = bookRepository.findById(bookId).orElseThrow();
        loanRepository.save(com.library.app.domain.Loan.builder()
                .reader(reader)
                .book(book)
                .borrowedAt(java.time.LocalDateTime.now())
                .dueAt(java.time.LocalDateTime.now().plusDays(14))
                .build());
    }

    private String createTokenForRole(String username, Role role) {
        User user = userRepository.save(User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode("pass123456"))
                .role(role)
                .readerStatus(role == Role.READER ? ReaderStatus.APPROVED : null)
                .build());
        return jwtService.generateToken(user.getUsername(), List.of("ROLE_" + role.name()));
    }
}