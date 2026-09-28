package com.dealflow.backend.exception;

public class DealExceptionNotFoundException extends RuntimeException {

    public DealExceptionNotFoundException(Long id) {
        super("Deal exception with id " + id + " was not found.");
    }
}