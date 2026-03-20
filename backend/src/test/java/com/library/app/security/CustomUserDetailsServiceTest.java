package com.library.app.security;

import com.library.app.domain.ReaderStatus;
import com.library.app.domain.Role;
import com.library.app.domain.User;
import com.library.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CustomUserDetailsServiceTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void loadUserByUsername_mapsReaderToRoleReader() {
		userRepository.save(User.builder()
				.username("reader1")
				.passwordHash(passwordEncoder.encode("secret"))
				.role(Role.READER)
				.readerStatus(ReaderStatus.PENDING)
				.build());

		UserDetails details = userDetailsService.loadUserByUsername("reader1");

		assertThat(details.getUsername()).isEqualTo("reader1");
		assertThat(passwordEncoder.matches("secret", details.getPassword())).isTrue();
		assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_READER");
	}

	@Test
	void loadUserByUsername_mapsAdminToRoleAdmin() {
		userRepository.save(User.builder()
				.username("admin1")
				.passwordHash(passwordEncoder.encode("adminpass"))
				.role(Role.ADMIN)
				.readerStatus(null)
				.build());

		UserDetails details = userDetailsService.loadUserByUsername("admin1");

		assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_ADMIN");
	}

	@Test
	void loadUserByUsername_unknown_throws() {
		assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nobody"))
				.isInstanceOf(UsernameNotFoundException.class);
	}
}
