package com.dealflow.backend.approval;

public final class ApprovalQueueMapper {

    private ApprovalQueueMapper() {
    }

    public static ApprovalQueueResponse toResponse(ApprovalRequest approval) {
        return new ApprovalQueueResponse(
                approval.getId(),
                approval.getDeal().getId(),
                approval.getDeal().getQuoteNumber(),
                approval.getDeal().getCustomerName(),
                approval.getDeal().getDealValue(),
                approval.getDeal().getDiscountPercentage(),
                approval.getDeal().getMarginPercentage(),
                approval.getDeal().getStatus(),
                approval.getApproverRole(),
                approval.getStatus(),
                approval.getSequence(),
                approval.getApprovalCycle(),
                approval.getRequestedAt()
        );
    }
}