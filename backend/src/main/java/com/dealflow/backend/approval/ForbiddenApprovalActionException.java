package com.dealflow.backend.approval;

public class ForbiddenApprovalActionException extends RuntimeException {

    public ForbiddenApprovalActionException(Long approvalId, ApprovalRole requiredRole) {
        super("Approval request " + approvalId + " requires role " + requiredRole + ".");
    }
}
