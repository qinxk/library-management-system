package com.library.app.service;

import com.library.app.domain.User;
import com.library.app.dto.MeResponse;
import com.library.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MeService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public MeResponse currentUser() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));
		String readerStatus = user.getReaderStatus() != null ? user.getReaderStatus().name() : null;
		return new MeResponse(user.getUsername(), user.getRole().name(), readerStatus);
	}
}
