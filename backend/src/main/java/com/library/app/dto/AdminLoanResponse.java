package com.library.app.dto;

import java.time.LocalDateTime;

public record AdminLoanResponse(
		Long id,
		String readerUsername,
		Long bookId,
		String bookTitle,
		String bookAuthor,
		LocalDateTime borrowedAt,
		LocalDateTime dueAt,
		LocalDateTime returnedAt
) {
}
