package com.gymflow.exception;

/**
 * Custom exception for validation errors.
 * Extends RuntimeException to allow it to be thrown without
 * forcing 'throws' declarations in every method signature.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}