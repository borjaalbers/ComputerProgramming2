package com.gymflow.exception;

/**
 * Exception thrown when database operations fail or data access errors occur.
 * 
 * <p>This exception wraps SQLException and other database-related errors,
 * providing a user-friendly message while preserving the underlying cause.</p>
 */
public class DataAccessException extends GymFlowException {
    
    /**
     * Constructs a new DataAccessException with the specified detail message.
     *
     * @param message the detail message
     */
    public DataAccessException(String message) {
        super(message);
    }

    /**
     * Constructs a new DataAccessException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

