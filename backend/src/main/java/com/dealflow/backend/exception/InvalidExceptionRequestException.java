package com.dealflow.backend.exception;

public class InvalidExceptionRequestException extends RuntimeException {

    public InvalidExceptionRequestException(String message) {
        super(message);
    }
}