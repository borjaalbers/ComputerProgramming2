package com.gymflow.exception;

/**
 * Exception thrown when input validation fails.
 * 
 * <p>This exception is used for validation errors such as invalid input formats,
 * missing required fields, or values that don't meet business rules.
 * This is an unchecked exception (RuntimeException) as validation errors
 * should be caught and handled early in the application flow.</p>
 */
public class ValidationException extends RuntimeException {
    
    /**
     * Constructs a new ValidationException with the specified detail message.
     *
     * @param message the detail message
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ValidationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

