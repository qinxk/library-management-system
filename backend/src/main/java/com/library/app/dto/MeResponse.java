package com.library.app.dto;

public record MeResponse(
		String username,
		String role,
		String readerStatus
) {
}
