package com.dealflow.backend.cpq;

import java.math.BigDecimal;

public record CpqQuoteResponse(
        String quoteNumber,
        String customerName,
        BigDecimal totalPrice,
        BigDecimal discountPercentage,
        BigDecimal marginPercentage,
        String transactionStatus
) {
}
