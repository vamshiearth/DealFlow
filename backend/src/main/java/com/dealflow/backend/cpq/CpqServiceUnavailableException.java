package com.dealflow.backend.cpq;

public class CpqServiceUnavailableException extends CpqException {

    public CpqServiceUnavailableException() {
        super("Oracle CPQ is temporarily unavailable.");
    }
}