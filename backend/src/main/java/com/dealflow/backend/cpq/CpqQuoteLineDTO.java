package com.dealflow.backend.cpq;

import java.math.BigDecimal;

public record CpqQuoteLineDTO(
        Integer lineNumber,
        String partNumber,
        String description,
        BigDecimal quantity,
        BigDecimal unitListPrice,
        BigDecimal unitNetPrice,
        BigDecimal extendedNetPrice
) {
}