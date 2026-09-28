package com.dealflow.backend.cpq;

public class CpqTimeoutException extends CpqException {

    public CpqTimeoutException() {
        super("Oracle CPQ did not respond within the allowed time.");
    }
}