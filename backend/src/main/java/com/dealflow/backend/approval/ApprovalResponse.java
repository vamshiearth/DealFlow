package com.dealflow.backend.approval;

import java.time.LocalDateTime;

public record ApprovalResponse(
        Long id,
        Long dealId,
        ApprovalRole approverRole,
        ApprovalStatus status,
        Integer sequence,
        Integer approvalCycle,
        LocalDateTime requestedAt,
        LocalDateTime resolvedAt,
        Long resolvedBy,
        String comments
) {
}