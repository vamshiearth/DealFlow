package com.dealflow.backend.exception;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record CreateExceptionRequest(
        @NotNull ExceptionType exceptionType,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal requestedValue,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal standardValue,
        @NotBlank String justification
) {
}