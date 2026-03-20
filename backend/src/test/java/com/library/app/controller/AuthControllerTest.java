package com.library.app.controller;

import com.library.app.domain.ReaderStatus;
import com.library.app.domain.Role;
import com.library.app.domain.User;
import com.library.app.dto.auth.LoginRequest;
import com.library.app.dto.auth.RegisterRequest;
import com.library.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void register_success_returnsTokenAndCreatesPendingReader() throws Exception {
        RegisterRequest request = new RegisterRequest("newreader", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newreader\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        User created = userRepository.findByUsername("newreader").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(created.getRole()).isEqualTo(Role.READER);
        org.assertj.core.api.Assertions.assertThat(created.getReaderStatus()).isEqualTo(ReaderStatus.PENDING);
    }

    @Test
    void register_duplicateUsername_returnsConflict() throws Exception {
        userRepository.save(User.builder()
                .username("exists")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.READER)
                .readerStatus(ReaderStatus.PENDING)
                .build());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"exists\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void login_success_returnsToken() throws Exception {
        userRepository.save(User.builder()
                .username("reader1")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.READER)
                .readerStatus(ReaderStatus.APPROVED)
                .build());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"reader1\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_wrongPassword_returnsUnauthorized() throws Exception {
        userRepository.save(User.builder()
                .username("reader2")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.READER)
                .readerStatus(ReaderStatus.APPROVED)
                .build());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"reader2\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isUnauthorized());
    }
}