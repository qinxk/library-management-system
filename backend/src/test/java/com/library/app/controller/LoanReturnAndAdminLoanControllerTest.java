package com.library.app.controller;

import com.library.app.domain.Book;
import com.library.app.domain.ReaderStatus;
import com.library.app.domain.Role;
import com.library.app.domain.User;
import com.library.app.repository.BookRepository;
import com.library.app.repository.LoanRepository;
import com.library.app.repository.UserRepository;
import com.library.app.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoanReturnAndAdminLoanControllerTest {

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

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void return_setsReturnedAt_andIncrementsAvailableCopies() throws Exception {
		User reader = saveReader("return_ok", ReaderStatus.APPROVED);
		Book book = bookRepository.save(Book.builder()
				.title("Return Me")
				.author("A")
				.totalCopies(2)
				.availableCopies(2)
				.build());

		String token = tokenFor(reader);
		String body = mockMvc.perform(post("/api/loans")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"bookId\": " + book.getId() + "}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		long loanId = objectMapper.readTree(body).get("id").asLong();

		mockMvc.perform(post("/api/loans/" + loanId + "/return")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.returnedAt").exists());

		assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableCopies()).isEqualTo(2);
		assertThat(loanRepository.findById(loanId).orElseThrow().getReturnedAt()).isNotNull();
	}

	@Test
	void return_secondCall_isIdempotent_forCopies() throws Exception {
		User reader = saveReader("return_twice", ReaderStatus.APPROVED);
		Book book = bookRepository.save(Book.builder()
				.title("Once")
				.author("A")
				.totalCopies(1)
				.availableCopies(1)
				.build());
		String token = tokenFor(reader);

		String json = mockMvc.perform(post("/api/loans")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"bookId\": " + book.getId() + "}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		long loanId = objectMapper.readTree(json).get("id").asLong();

		mockMvc.perform(post("/api/loans/" + loanId + "/return")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/loans/" + loanId + "/return")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		assertThat(bookRepository.findById(book.getId()).orElseThrow().getAvailableCopies()).isEqualTo(1);
	}

	@Test
	void return_wrongReader_returnsForbidden() throws Exception {
		User owner = saveReader("loan_owner", ReaderStatus.APPROVED);
		User other = saveReader("loan_other", ReaderStatus.APPROVED);
		Book book = bookRepository.save(Book.builder()
				.title("Owned")
				.author("A")
				.totalCopies(1)
				.availableCopies(1)
				.build());

		String ownerJson = mockMvc.perform(post("/api/loans")
						.header("Authorization", "Bearer " + tokenFor(owner))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"bookId\": " + book.getId() + "}"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		long loanId = objectMapper.readTree(ownerJson).get("id").asLong();

		mockMvc.perform(post("/api/loans/" + loanId + "/return")
						.header("Authorization", "Bearer " + tokenFor(other)))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminCanListAllLoans() throws Exception {
		User reader = saveReader("listed_reader", ReaderStatus.APPROVED);
		Book book = bookRepository.save(Book.builder()
				.title("Listed")
				.author("A")
				.totalCopies(1)
				.availableCopies(1)
				.build());

		mockMvc.perform(post("/api/loans")
						.header("Authorization", "Bearer " + tokenFor(reader))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"bookId\": " + book.getId() + "}"));

		userRepository.save(User.builder()
				.username("admin_loans")
				.passwordHash(passwordEncoder.encode("pass123456"))
				.role(Role.ADMIN)
				.build());
		String adminToken = jwtService.generateToken("admin_loans", List.of("ROLE_" + Role.ADMIN.name()));

		mockMvc.perform(get("/api/admin/loans")
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].readerUsername").value("listed_reader"))
				.andExpect(jsonPath("$[0].bookTitle").value("Listed"));
	}

	@Test
	void readerCannotAccessAdminLoans() throws Exception {
		User reader = saveReader("not_admin", ReaderStatus.APPROVED);
		mockMvc.perform(get("/api/admin/loans")
						.header("Authorization", "Bearer " + tokenFor(reader)))
				.andExpect(status().isForbidden());
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
