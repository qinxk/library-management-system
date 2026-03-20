package com.library.app.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "replace-with-at-least-32-characters-demo-secret-key",
            3600000
    );

    @Test
    void generateAndParseToken() {
        String token = jwtService.generateToken("reader1", List.of("ROLE_READER"));

        assertThat(jwtService.extractUsername(token)).isEqualTo("reader1");
        assertThat(jwtService.extractRoles(token)).containsExactly("ROLE_READER");
        assertThat(jwtService.isTokenExpired(token)).isFalse();
    }

    @Test
    void tokenValidatesAgainstUserDetails() {
        UserDetails userDetails = User.withUsername("admin1")
                .password("noop")
                .authorities("ROLE_ADMIN")
                .build();

        String token = jwtService.generateToken("admin1", List.of("ROLE_ADMIN"));

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void tokenExpiresWhenConfiguredVeryShort() throws InterruptedException {
        JwtService shortLived = new JwtService(
                "replace-with-at-least-32-characters-demo-secret-key",
                1
        );

        String token = shortLived.generateToken("u", List.of("ROLE_READER"));
        Thread.sleep(5);

        assertThat(shortLived.isTokenExpired(token)).isTrue();
    }
}