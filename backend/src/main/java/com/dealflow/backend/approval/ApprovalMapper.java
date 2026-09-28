package com.dealflow.backend.approval;

public final class ApprovalMapper {

    private ApprovalMapper() {
    }

    public static ApprovalResponse toResponse(ApprovalRequest request) {
        return new ApprovalResponse(
                request.getId(),
                request.getDeal().getId(),
                request.getApproverRole(),
                request.getStatus(),
                request.getSequence(),
                request.getApprovalCycle(),
                request.getRequestedAt(),
                request.getResolvedAt(),
                request.getResolvedBy(),
                request.getComments()
        );
    }
}