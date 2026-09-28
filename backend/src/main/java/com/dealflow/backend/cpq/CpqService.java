package com.dealflow.backend.cpq;

import org.springframework.stereotype.Service;

@Service
public class CpqService {

    private final CpqClient cpqClient;

    public CpqService(CpqClient cpqClient) {
        this.cpqClient = cpqClient;
    }

    public CpqQuoteDTO getQuote(String quoteNumber) {
        CpqQuoteResponse response = cpqClient.getQuote(quoteNumber);
        validateQuoteResponse(response);

        return CpqQuoteMapper.toDto(response);
    }

    public CpqQuoteDetailDTO getQuoteDetails(String quoteNumber) {
        CpqQuoteDetailResponse response = cpqClient.getQuoteDetails(quoteNumber);
        validateQuoteDetailResponse(response);

        return CpqQuoteMapper.toDetailDto(response);
    }

    private void validateQuoteResponse(CpqQuoteResponse response) {
        if (response == null) {
            throw new CpqInvalidResponseException("response body was empty");
        }
        if (isBlank(response.quoteNumber())) {
            throw new CpqInvalidResponseException("quote number is missing");
        }
        if (isBlank(response.customerName())) {
            throw new CpqInvalidResponseException("customer name is missing");
        }
        if (response.totalPrice() == null) {
            throw new CpqInvalidResponseException("total price is missing");
        }
        if (response.discountPercentage() == null) {
            throw new CpqInvalidResponseException("discount percentage is missing");
        }
        if (response.marginPercentage() == null) {
            throw new CpqInvalidResponseException("margin percentage is missing");
        }
    }

    private void validateQuoteDetailResponse(CpqQuoteDetailResponse response) {
        if (response == null) {
            throw new CpqInvalidResponseException("quote detail response was empty");
        }
        if (isBlank(response.quoteNumber())) {
            throw new CpqInvalidResponseException("quote number is missing");
        }
        if (isBlank(response.customerName())) {
            throw new CpqInvalidResponseException("customer name is missing");
        }
        if (response.pricing() == null) {
            throw new CpqInvalidResponseException("pricing information is missing");
        }
        if (response.configuration() == null) {
            throw new CpqInvalidResponseException("configuration information is missing");
        }
        if (response.quoteLines() == null) {
            throw new CpqInvalidResponseException("quote lines are missing");
        }
        if (response.bom() == null) {
            throw new CpqInvalidResponseException("BOM information is missing");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
