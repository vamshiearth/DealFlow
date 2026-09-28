package com.dealflow.backend.deal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DealResponse(
        Long id,
        String quoteNumber,
        String customerName,
        BigDecimal dealValue,
        BigDecimal discountPercentage,
        BigDecimal marginPercentage,
        DealStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}