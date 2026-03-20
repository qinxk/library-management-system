package com.library.app.controller;

import com.library.app.domain.ReaderStatus;
import com.library.app.domain.Role;
import com.library.app.domain.User;
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

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtService jwtService;

	@Test
	void me_withReaderToken_returnsRoleAndReaderStatus() throws Exception {
		userRepository.save(User.builder()
				.username("me_reader")
				.passwordHash(passwordEncoder.encode("pass123456"))
				.role(Role.READER)
				.readerStatus(ReaderStatus.APPROVED)
				.build());
		String token = jwtService.generateToken("me_reader", List.of("ROLE_READER"));

		mockMvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("me_reader"))
				.andExpect(jsonPath("$.role").value("READER"))
				.andExpect(jsonPath("$.readerStatus").value("APPROVED"));
	}

	@Test
	void me_withAdminToken_returnsRoleAndNullReaderStatus() throws Exception {
		userRepository.save(User.builder()
				.username("me_admin")
				.passwordHash(passwordEncoder.encode("pass123456"))
				.role(Role.ADMIN)
				.build());
		String token = jwtService.generateToken("me_admin", List.of("ROLE_ADMIN"));

		mockMvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("me_admin"))
				.andExpect(jsonPath("$.role").value("ADMIN"))
				.andExpect(jsonPath("$.readerStatus").value(nullValue()));
	}

	@Test
	void me_withoutToken_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/me"))
				.andExpect(status().isUnauthorized());
	}
}
