package com.careerpilot.auth.exception;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.careerpilot.auth.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
        LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException exception,
        HttpServletRequest request){
            ErrorResponse response = new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                HttpStatus.CONFLICT.value(),
                "EMAIL_ALREADY_EXISTS",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
            );

            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException exception, HttpServletRequest request){
        ErrorResponse response = new ErrorResponse(
            OffsetDateTime.now(ZoneOffset.UTC),
            HttpStatus.UNAUTHORIZED.value(),
            "INVALID_CREDENTIALS",
            exception.getMessage(),
            request.getRequestURI(),
            Map.of()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request){
        Map<String, String> fieldErrors = exception.getBindingResult()
        .getFieldErrors().stream()
        .collect(Collectors.toMap(FieldError::getField,
        FieldError::getDefaultMessage,
        (existing, replacement) -> existing));

        String message = "Request validation failed";

        ErrorResponse response = new ErrorResponse(
            OffsetDateTime.now(ZoneOffset.UTC),
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_ERROR",
            message,
            request.getRequestURI(),
            fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericResponse(Exception exception, HttpServletRequest request){

        log.error(
            "Unexpected error occurred while processing request: {}",
            request.getRequestURI(),
            exception
    );

        ErrorResponse response = new ErrorResponse(
            OffsetDateTime.now(ZoneOffset.UTC),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            request.getRequestURI(),
            Map.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
