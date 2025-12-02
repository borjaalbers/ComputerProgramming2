package com.gymflow.exception;

/**
 * Exception thrown when file I/O operations fail.
 * 
 * <p>This exception is used for file-related errors such as file not found,
 * permission denied, invalid file format, or I/O errors during read/write operations.</p>
 */
public class FileOperationException extends GymFlowException {
    
    /**
     * Constructs a new FileOperationException with the specified detail message.
     *
     * @param message the detail message
     */
    public FileOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileOperationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}

