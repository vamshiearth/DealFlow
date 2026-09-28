package com.dealflow.backend.cpq;

import java.math.BigDecimal;

public record CpqBomItemDTO(
        String itemNumber,
        String parentItemNumber,
        String partNumber,
        String description,
        BigDecimal quantity
) {
}