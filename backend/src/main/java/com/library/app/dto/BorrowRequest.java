package com.library.app.dto;

import jakarta.validation.constraints.NotNull;

public record BorrowRequest(@NotNull Long bookId) {
}
