package com.library.app.dto.user;

import com.library.app.domain.ReaderStatus;

public record PendingReaderResponse(
        Long id,
        String username,
        ReaderStatus readerStatus
) {
}