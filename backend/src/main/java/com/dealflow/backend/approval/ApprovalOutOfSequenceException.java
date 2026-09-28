package com.dealflow.backend.approval;

public class ApprovalOutOfSequenceException extends RuntimeException {

    public ApprovalOutOfSequenceException(Long approvalId, Integer expectedSequence) {
        super(
                "Approval request "
                        + approvalId
                        + " cannot be processed yet. Approval sequence "
                        + expectedSequence
                        + " must be completed first."
        );
    }
}