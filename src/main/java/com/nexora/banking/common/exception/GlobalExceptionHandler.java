package com.nexora.banking.common.exception;

import com.nexora.banking.common.response.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed.");

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "VALIDATION_ERROR",
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<ApiErrorResponse> handleEmailAlreadyExists(
                EmailAlreadyExistsException ex,
                HttpServletRequest request
        ) {

        HttpStatus status = HttpStatus.CONFLICT;

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                "EMAIL_ALREADY_EXISTS",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(status)
                .body(response);
     }

     
     @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
                InvalidCredentialsException ex,
                HttpServletRequest request
        ) {

        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                "INVALID_CREDENTIALS",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(status)
                .body(response);
      }

}