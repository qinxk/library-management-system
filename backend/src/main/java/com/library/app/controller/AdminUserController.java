package com.library.app.controller;

import com.library.app.dto.user.PendingReaderResponse;
import com.library.app.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/pending")
    public List<PendingReaderResponse> listPendingReaders() {
        return adminUserService.listPendingReaders();
    }

    @PostMapping("/{id}/approve")
    @ResponseStatus(NO_CONTENT)
    public void approveReader(@PathVariable Long id) {
        adminUserService.approveReader(id);
    }

    @PostMapping("/{id}/reject")
    @ResponseStatus(NO_CONTENT)
    public void rejectReader(@PathVariable Long id) {
        adminUserService.rejectReader(id);
    }
}