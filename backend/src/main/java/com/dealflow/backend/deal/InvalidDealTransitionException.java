package com.dealflow.backend.deal;

public class InvalidDealTransitionException extends RuntimeException {

    public InvalidDealTransitionException(
            DealStatus currentStatus,
            DealStatus requestedStatus) {
        super(
                "Cannot transition deal from "
                        + currentStatus
                        + " to "
                        + requestedStatus
                        + "."
        );
    }
}