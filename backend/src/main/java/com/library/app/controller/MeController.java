package com.library.app.controller;

import com.library.app.dto.MeResponse;
import com.library.app.service.MeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

	private final MeService meService;

	@GetMapping
	public MeResponse me() {
		return meService.currentUser();
	}
}
