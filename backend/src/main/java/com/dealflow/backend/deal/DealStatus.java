package com.dealflow.backend.deal;

public enum DealStatus {

    DRAFT,
    SUBMITTED,
    MANAGER_REVIEW,
    FINANCE_REVIEW,
    VP_REVIEW,
    APPROVED,
    REJECTED,
    CHANGES_REQUESTED,
    CANCELLED
}