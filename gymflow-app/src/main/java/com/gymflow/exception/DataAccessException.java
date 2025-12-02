package com.gymflow.exception;

/**
 * Custom exception for data access errors.
 * Extends RuntimeException to be "Unchecked", which prevents
 * interface conflict errors in the Service layer.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}