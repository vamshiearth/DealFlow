package com.dealflow.backend.cpq;

public class CpqException extends RuntimeException {

    public CpqException(String message) {
        super(message);
    }

    public CpqException(String message, Throwable cause) {
        super(message, cause);
    }
}
