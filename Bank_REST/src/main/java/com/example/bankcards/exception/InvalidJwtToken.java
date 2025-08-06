package com.example.bankcards.exception;

public class InvalidJwtToken extends RuntimeException {
    public InvalidJwtToken(String message) {
        super(message);
    }

    public InvalidJwtToken(String message, Throwable cause) {
        super(message, cause);
    }
}
