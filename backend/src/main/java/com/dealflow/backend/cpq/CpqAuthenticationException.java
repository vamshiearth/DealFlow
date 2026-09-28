package com.dealflow.backend.cpq;

public class CpqAuthenticationException extends CpqException {

    public CpqAuthenticationException() {
        super("Oracle CPQ authentication failed.");
    }
}