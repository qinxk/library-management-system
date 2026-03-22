package com.library.app.service;

import com.library.app.domain.ReaderStatus;
import com.library.app.domain.Role;
import com.library.app.domain.User;
import com.library.app.dto.user.ChangePasswordRequest;
import com.library.app.dto.user.PendingReaderResponse;
import com.library.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<PendingReaderResponse> listPendingReaders() {
        return userRepository.findByRoleAndReaderStatusOrderByIdAsc(Role.READER, ReaderStatus.PENDING)
                .stream()
                .map(this::toPendingReaderResponse)
                .toList();
    }

    public void approveReader(Long userId) {
        User user = findReader(userId);
        user.setReaderStatus(ReaderStatus.APPROVED);
        userRepository.save(user);
    }

    public void rejectReader(Long userId) {
        User user = findReader(userId);
        user.setReaderStatus(ReaderStatus.REJECTED);
        userRepository.save(user);
    }

    private User findReader(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != Role.READER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only reader can be reviewed");
        }
        return user;
    }

    private PendingReaderResponse toPendingReaderResponse(User user) {
        return new PendingReaderResponse(user.getId(), user.getUsername(), user.getReaderStatus());
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    public List<User> listAllUsers() {
        return userRepository.findAll();
    }
}