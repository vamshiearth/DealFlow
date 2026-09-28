package com.dealflow.backend.cpq;

public interface CpqClient {

    CpqQuoteResponse getQuote(String quoteNumber);

    CpqQuoteDetailResponse getQuoteDetails(String quoteNumber);
}
