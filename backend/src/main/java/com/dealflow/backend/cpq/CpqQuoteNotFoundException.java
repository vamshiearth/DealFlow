package com.dealflow.backend.cpq;

public class CpqQuoteNotFoundException extends CpqException {

    public CpqQuoteNotFoundException(String quoteNumber) {
        super("CPQ quote " + quoteNumber + " was not found.");
    }
}