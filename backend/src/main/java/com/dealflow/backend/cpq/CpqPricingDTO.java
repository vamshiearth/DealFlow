package com.dealflow.backend.cpq;

import java.math.BigDecimal;

public record CpqPricingDTO(
        BigDecimal listPrice,
        BigDecimal discountPercentage,
        BigDecimal discountAmount,
        BigDecimal netPrice,
        BigDecimal marginPercentage
) {
}