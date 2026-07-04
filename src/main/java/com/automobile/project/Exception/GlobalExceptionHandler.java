package com.automobile.project.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

/**
 * Catches RuntimeExceptions thrown from services/controllers (e.g. "Vehicle not found",
 * "Email already exists") and turns them into clean JSON error responses instead of
 * a raw Whitelabel 500 stack trace page.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicatModelException.class)
    public ResponseEntity<?> handleDuplicateModel(DuplicatModelException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        StringBuilder sb = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                sb.append(err.getField()).append(": ").append(err.getDefaultMessage()).append("; "));
        return buildResponse(HttpStatus.BAD_REQUEST,
                sb.length() > 0 ? sb.toString() : "Validation failed.");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<?> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message != null ? message : "Something went wrong.");
        body.put("status", status.value());
        return ResponseEntity.status(status).body(body);
    }
}
