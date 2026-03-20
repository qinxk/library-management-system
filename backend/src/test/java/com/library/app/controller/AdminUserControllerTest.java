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

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void adminCanListPendingReadersOnly() throws Exception {
        String adminToken = createTokenForRole("admin_users", Role.ADMIN);

        userRepository.save(User.builder()
                .username("reader_pending")
                .passwordHash(passwordEncoder.encode("pass123456"))
                .role(Role.READER)
                .readerStatus(ReaderStatus.PENDING)
                .build());
        userRepository.save(User.builder()
                .username("reader_approved")
                .passwordHash(passwordEncoder.encode("pass123456"))
                .role(Role.READER)
                .readerStatus(ReaderStatus.APPROVED)
                .build());

        mockMvc.perform(get("/api/admin/users/pending")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("reader_pending"))
                .andExpect(jsonPath("$[0].readerStatus").value("PENDING"));
    }

    @Test
    void adminCanApproveAndRejectReader() throws Exception {
        String adminToken = createTokenForRole("admin_review", Role.ADMIN);
        User target = userRepository.save(User.builder()
                .username("reader_review")
                .passwordHash(passwordEncoder.encode("pass123456"))
                .role(Role.READER)
                .readerStatus(ReaderStatus.PENDING)
                .build());

        mockMvc.perform(post("/api/admin/users/{id}/approve", target.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        User approved = userRepository.findById(target.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(approved.getReaderStatus()).isEqualTo(ReaderStatus.APPROVED);

        mockMvc.perform(post("/api/admin/users/{id}/reject", target.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        User rejected = userRepository.findById(target.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(rejected.getReaderStatus()).isEqualTo(ReaderStatus.REJECTED);
    }

    @Test
    void readerCannotAccessAdminReviewApi() throws Exception {
        String readerToken = createTokenForRole("reader_no_admin", Role.READER);

        mockMvc.perform(get("/api/admin/users/pending")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isForbidden());
    }

    private String createTokenForRole(String username, Role role) {
        userRepository.save(User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode("pass123456"))
                .role(role)
                .readerStatus(role == Role.READER ? ReaderStatus.APPROVED : null)
                .build());
        return jwtService.generateToken(username, List.of("ROLE_" + role.name()));
    }
}