package com.library.app.config;

import com.library.app.domain.Role;
import com.library.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:seedtest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "app.seed.admin-username=seed_admin",
        "app.seed.admin-password=seed_password"
})
class DataInitializerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createsAdminUserOnStartupWhenMissing() {
        var admin = userRepository.findByUsername("seed_admin");

        assertThat(admin).isPresent();
        assertThat(admin.orElseThrow().getRole()).isEqualTo(Role.ADMIN);
        assertThat(passwordEncoder.matches("seed_password", admin.orElseThrow().getPasswordHash())).isTrue();
    }
}