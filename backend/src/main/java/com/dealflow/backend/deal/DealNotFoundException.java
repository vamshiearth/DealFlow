package com.dealflow.backend.deal;

public class DealNotFoundException extends RuntimeException {

    public DealNotFoundException(Long id) {
        super("Deal with id " + id + " was not found.");
    }
}