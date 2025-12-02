package com.gymflow.exception;

/**
 * Exception thrown when authentication fails or user credentials are invalid.
 * 
 * <p>This exception is used for login failures, invalid credentials, and
 * other authentication-related errors.</p>
 */
public class AuthenticationException extends GymFlowException {
    
    /**
     * Constructs a new AuthenticationException with the specified detail message.
     *
     * @param message the detail message
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Constructs a new AuthenticationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

