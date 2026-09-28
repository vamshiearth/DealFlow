package com.dealflow.backend.exception;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExceptionResponse(
        Long id,
        Long dealId,
        ExceptionType exceptionType,
        BigDecimal requestedValue,
        BigDecimal standardValue,
        String justification,
        ExceptionStatus status,
        Long createdBy,
        Long resolvedBy,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
}