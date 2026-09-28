package com.dealflow.backend.approval;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.dealflow.backend.deal.DealStatus;

public record ApprovalQueueResponse(
        Long approvalId,
        Long dealId,
        String quoteNumber,
        String customerName,
        BigDecimal dealValue,
        BigDecimal discountPercentage,
        BigDecimal marginPercentage,
        DealStatus dealStatus,
        ApprovalRole approverRole,
        ApprovalStatus approvalStatus,
        Integer sequence,
        Integer approvalCycle,
        LocalDateTime requestedAt
) {
}