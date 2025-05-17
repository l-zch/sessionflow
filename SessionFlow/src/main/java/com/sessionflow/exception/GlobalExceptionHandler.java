package com.sessionflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.NOT_FOUND.value();
        errorResponse.message = ex.getMessage();
        errorResponse.timestamp = LocalDateTime.now();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse errorResponse = new ValidationErrorResponse();
        errorResponse.status = HttpStatus.BAD_REQUEST.value();
        errorResponse.message = "Validation failed";
        errorResponse.timestamp = LocalDateTime.now();
        errorResponse.errors = errors;
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        errorResponse.message = "An unexpected error occurred: " + ex.getMessage();
        errorResponse.timestamp = LocalDateTime.now();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static class ErrorResponse {
        public int status;
        public String message;
        public LocalDateTime timestamp;
    }

    public static class ValidationErrorResponse {
        public int status;
        public String message;
        public LocalDateTime timestamp;
        public Map<String, String> errors;
    }
}