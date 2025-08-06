package com.example.bankcards.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    record ErrorResponse(int status, String error, String path) {
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            String field = path.substring(path.lastIndexOf('.') + 1);
            errors.put(field, violation.getMessage());
        });
        log.warn("Constraint violation: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(InvalidJwtToken.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidJwt
            (InvalidJwtToken ex, HttpServletRequest req) {
        log.warn("JWT validation failed for request [{}]: {}", req.getRequestURI(), ex.getMessage());
        return new ErrorResponse(401, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadCredentials
            (BadCredentialsException ex, HttpServletRequest req) {
        log.info("Authentication failure for [{}]: {}", req.getRequestURI(), ex.getMessage());
        return new ErrorResponse(400, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound
            (UsernameNotFoundException ex, HttpServletRequest req) {
        log.info("User not found during request [{}]: {}", req.getRequestURI(), ex.getMessage());
        return new ErrorResponse(404, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(InternalServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalError
            (InternalServerException ex, HttpServletRequest req) {
        log.error("Internal server error at [{}]: {}", req.getRequestURI(), ex.getMessage(), ex);
        return new ErrorResponse(500, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException
            (IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("User inserted illegal argument at [{}]: {}", req.getRequestURI(), ex.getMessage(), ex);
        return new ErrorResponse(400, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAll
            (Exception ex, HttpServletRequest req) {
        log.error("Unexpected error at [{}]: {}", req.getRequestURI(), ex.getMessage(), ex);
        return new ErrorResponse(500, "Unexpected error: " + ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handelMethodValidation
            (HandlerMethodValidationException e, HttpServletRequest req){
        log.warn("User tried to fetch with invalid argument at [{}]: {}", req.getRequestURI(), e.getMessage(), e);
        return new ErrorResponse(400, "Validation error with the parameters: " + e.getMessage(), req.getRequestURI());
    }
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException
            (NotFoundException ex, HttpServletRequest req) {
        log.info("User tried to fetch not found resource [{}]: {}", req.getRequestURI(), ex.getMessage(), ex);
        return new ErrorResponse(404, "Not found: " + ex.getMessage(), req.getRequestURI());
    }
}
