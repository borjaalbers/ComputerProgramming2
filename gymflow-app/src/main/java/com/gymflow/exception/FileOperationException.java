package com.gymflow.exception;

/**
 * Custom exception for file operation errors.
 * Extends RuntimeException to avoid strict interface declaration requirements.
 */
public class FileOperationException extends RuntimeException {
    public FileOperationException(String message) {
        super(message);
    }

    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}