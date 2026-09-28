package com.dealflow.backend.exception;

import java.math.BigDecimal;

public class ExceptionApprovalRequiredException extends RuntimeException {

    public ExceptionApprovalRequiredException(
            ExceptionType exceptionType,
            BigDecimal requestedValue,
            BigDecimal standardValue) {
        super(
                "Approved " + exceptionType
                        + " exception is required. Requested value: "
                        + requestedValue
                        + ", standard value: "
                        + standardValue
                        + "."
        );
    }
}