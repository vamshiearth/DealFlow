package com.dealflow.backend.cpq;

import java.math.BigDecimal;

public record CpqQuoteDTO(
        String quoteNumber,
        String customerName,
        BigDecimal totalPrice,
        BigDecimal discount,
        BigDecimal margin,
        String status
) {
}
