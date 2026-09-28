package com.dealflow.backend.exception;

import java.math.BigDecimal;

public record ExceptionRequirementResponse(
        ExceptionType exceptionType,
        BigDecimal requestedValue,
        BigDecimal standardValue,
        boolean satisfied,
        Long exceptionId,
        ExceptionStatus exceptionStatus
) {
}