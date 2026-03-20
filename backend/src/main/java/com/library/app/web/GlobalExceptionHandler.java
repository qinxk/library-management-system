package com.library.app.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST, "Validation failed");
		problem.setTitle("Bad Request");

		List<Map<String, String>> fieldErrors = new ArrayList<>();
		for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
			Map<String, String> row = new LinkedHashMap<>();
			row.put("field", fe.getField());
			row.put("message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "");
			fieldErrors.add(row);
		}
		problem.setProperty("fieldErrors", fieldErrors);

		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Conflict");
		problem.setProperty("code", ex.getCode());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
	}
}
