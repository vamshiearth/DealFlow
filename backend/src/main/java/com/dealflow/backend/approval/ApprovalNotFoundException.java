package com.dealflow.backend.approval;

public class ApprovalNotFoundException extends RuntimeException {

    public ApprovalNotFoundException(Long id) {
        super("Approval request with id " + id + " was not found.");
    }
}