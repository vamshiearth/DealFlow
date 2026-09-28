package com.dealflow.backend.cpq;

public class CpqInvalidResponseException extends CpqException {

    public CpqInvalidResponseException(String message) {
        super("Invalid Oracle CPQ response: " + message);
    }
}