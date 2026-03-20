package com.library.app.controller;

import com.library.app.dto.AdminLoanResponse;
import com.library.app.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/loans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLoanController {

	private final LoanService loanService;

	@GetMapping
	public List<AdminLoanResponse> listAll() {
		return loanService.listAllLoansForAdmin();
	}
}
