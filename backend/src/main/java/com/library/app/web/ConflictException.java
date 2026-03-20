package com.library.app.web;

import lombok.Getter;

/**
 * Business rule conflict (HTTP 409); handled by {@link GlobalExceptionHandler}.
 */
@Getter
public class ConflictException extends RuntimeException {

	private final String code;

	public ConflictException(String code, String message) {
		super(message);
		this.code = code;
	}
}
