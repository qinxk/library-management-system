package com.library.app.dto.book;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        String category,
        String description,
        int totalCopies,
        int availableCopies
) {
}