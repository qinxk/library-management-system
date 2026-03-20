package com.library.app.controller;

import com.library.app.dto.BorrowRequest;
import com.library.app.dto.LoanResponse;
import com.library.app.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LoanController {

	private final LoanService loanService;

	@PostMapping("/api/loans")
	@PreAuthorize("hasRole('READER')")
	public LoanResponse borrow(@Valid @RequestBody BorrowRequest request) {
		return loanService.borrow(request.bookId());
	}

	@GetMapping("/api/me/loans")
	public List<LoanResponse> myLoans() {
		return loanService.listMyLoans();
	}
}
