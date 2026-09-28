package com.dealflow.backend.deal;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateDealRequest(
        @NotBlank String customerName,
        @NotNull @Positive BigDecimal dealValue,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal discountPercentage,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal marginPercentage
) {
}