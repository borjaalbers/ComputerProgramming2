package com.gymflow.exception;

/**
 * Base exception class for all GymFlow application exceptions.
 * 
 * <p>This class provides a common base for all custom exceptions in the GymFlow
 * application, allowing for consistent error handling and user-friendly messages.</p>
 */
public class GymFlowException extends Exception {
    
    /**
     * Constructs a new GymFlowException with the specified detail message.
     *
     * @param message the detail message
     */
    public GymFlowException(String message) {
        super(message);
    }

    /**
     * Constructs a new GymFlowException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public GymFlowException(String message, Throwable cause) {
        super(message, cause);
    }
}

