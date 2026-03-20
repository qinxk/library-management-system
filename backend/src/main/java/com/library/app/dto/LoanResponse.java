package com.library.app.dto;

import java.time.LocalDateTime;

public record LoanResponse(
		Long id,
		Long bookId,
		String bookTitle,
		String bookAuthor,
		LocalDateTime borrowedAt,
		LocalDateTime dueAt,
		LocalDateTime returnedAt
) {
}
