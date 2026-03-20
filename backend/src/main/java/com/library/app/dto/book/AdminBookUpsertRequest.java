package com.library.app.dto.book;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminBookUpsertRequest(
        @NotBlank @Size(max = 512) String title,
        @NotBlank @Size(max = 256) String author,
        @Size(max = 32) String isbn,
        @Size(max = 128) String category,
        @Size(max = 2000) String description,
        @Min(0) @Max(1000000) int totalCopies,
        @Min(0) @Max(1000000) int availableCopies
) {
}