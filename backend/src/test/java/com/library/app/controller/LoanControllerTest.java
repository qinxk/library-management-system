package com.library.app.controller;

import com.library.app.domain.Book;
import com.library.app.domain.ReaderStatus;
import com.library.app.domain.Role;
import com.library.app.domain.User;
import com.library.app.repository.BookRepository;
import com.library.app.repository.LoanRepository;
import com.library.app.repository.UserRepository;
import com.library.app.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoanControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private LoanRepository loanRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtService jwtService;

	@Test
	void approvedReaderBorrow_decrementsAvailableAndCreatesLoan() throws Exception {
		User reader = saveReader("borrow_ok", ReaderStatus.APPROVED);
		Book book = bookRepository.save(Book.builder()
				.title("Designing Data-Intensive Applications")
				.author("Martin Kleppmann")
				.totalCopies(3)
				.availableCopies(3)
				.build());

		mockMvc.perform(post("/api/loans")
						.header("Authorization", "Bearer " + tokenFor(reader))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"bookId\": " + book.getId() + "}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.bookId").value(book.getId().intValue()))
				.andExpect(jsonPath("$.bookTitle").value("Designing Data-Intensive Applications"))
				.andExpect(jsonPath("$.returnedAt").value(nullValue()));

		Book updated = bookRepository.findById(book.getId()).orElseThrow();
		assertThat(updated.getAvailableCopies()).isEqualTo(2);
		assertThat(loanRepository.count()).isEqualTo(1);
	}

	@Test
	void pendingReaderBorrow_returnsForbiddenWithReason() throws Exception {
		User reader = saveReader("borrow_pending", ReaderStatus.PENDING);
		Book book = bookRepository.save(Book.builder()
				.title("Pending Book")
				.author("A")
				.totalCopies(1)
				.availableCopies(1)
				.build());

		mockMvc.perform(post("/api/loans")
						.header("Authorization", "Bearer " + tokenFor(reader))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"bookId\": " + book.getId() + "}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void duplicateActiveLoanSameBook_returnsConflict() throws Exception {
		User reader = saveReader("borrow_dup", ReaderStatus.APPROVED);
		Book book = bookRepository.save(Book.builder()
				.title("Dup")
				.author("A")
				.totalCopies(2)
				.availableCopies(2)
				.build());
		String token = tokenFor(reader);

		mockMvc.perform(post("/api/loans")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"bookId\": " + book.getId() + "}"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/loans")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"bookId\": " + book.getId() + "}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("DUPLICATE_ACTIVE_LOAN"));
	}

	@Test
	void exceedsMaxActiveLoans_returnsConflict() throws Exception {
		User reader = saveReader("borrow_cap", ReaderStatus.APPROVED);
		String token = tokenFor(reader);
		for (int i = 0; i < 5; i++) {
			Book book = bookRepository.save(Book.builder()
					.title("Vol " + i)
					.author("A")
					.totalCopies(1)
					.availableCopies(1)
					.build());
			mockMvc.perform(post("/api/loans")
							.header("Authorization", "Bearer " + token)
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"bookId\": " + book.getId() + "}"))
					.andExpect(status().isOk());
		}

		Book extra = bookRepository.save(Book.builder()
				.title("Overflow")
				.author("A")
				.totalCopies(1)
				.availableCopies(1)
				.build());

		mockMvc.perform(post("/api/loans")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"bookId\": " + extra.getId() + "}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("MAX_ACTIVE_LOANS"));
	}

	@Test
	void myLoans_returnsListForReader() throws Exception {
		User reader = saveReader("me_loans", ReaderStatus.APPROVED);
		Book book = bookRepository.save(Book.builder()
				.title("Mine")
				.author("A")
				.totalCopies(1)
				.availableCopies(1)
				.build());

		mockMvc.perform(post("/api/loans")
						.header("Authorization", "Bearer " + tokenFor(reader))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"bookId\": " + book.getId() + "}"));

		mockMvc.perform(get("/api/me/loans")
						.header("Authorization", "Bearer " + tokenFor(reader)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].bookTitle").value("Mine"));
	}

	private User saveReader(String username, ReaderStatus status) {
		return userRepository.save(User.builder()
				.username(username)
				.passwordHash(passwordEncoder.encode("pass123456"))
				.role(Role.READER)
				.readerStatus(status)
				.build());
	}

	private String tokenFor(User user) {
		return jwtService.generateToken(user.getUsername(), List.of("ROLE_" + user.getRole().name()));
	}
}
