package com.library.app.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {
        SecurityIntegrationTest.TestApiController.class,
        com.library.app.LibraryBackendApplication.class
})
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousCanAccessBooksRoute() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousCannotAccessProtectedRoute() throws Exception {
        mockMvc.perform(get("/api/me/loans"))
                .andExpect(status().isUnauthorized());
    }

    @RestController
    static class TestApiController {

        @GetMapping("/api/books")
        ResponseEntity<String> books() {
            return ResponseEntity.ok("[]");
        }

        @GetMapping("/api/me/loans")
        ResponseEntity<String> myLoans() {
            return ResponseEntity.ok("[]");
        }
    }

}