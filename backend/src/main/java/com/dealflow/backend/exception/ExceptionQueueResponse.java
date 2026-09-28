package com.dealflow.backend.exception;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.dealflow.backend.deal.DealStatus;

public record ExceptionQueueResponse(
        Long exceptionId,
        Long dealId,
        String quoteNumber,
        String customerName,
        BigDecimal dealValue,
        BigDecimal discountPercentage,
        BigDecimal marginPercentage,
        DealStatus dealStatus,
        ExceptionType exceptionType,
        BigDecimal requestedValue,
        BigDecimal standardValue,
        String justification,
        ExceptionStatus exceptionStatus,
        LocalDateTime createdAt
) {
}