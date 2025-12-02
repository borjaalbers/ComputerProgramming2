package com.gymflow.exception;

public class GymFlowException extends Exception {
    public GymFlowException(String message) {
        super(message);
    }

    public GymFlowException(String message, Throwable cause) {
        super(message, cause);
    }
}
