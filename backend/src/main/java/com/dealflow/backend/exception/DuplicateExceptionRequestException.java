package com.dealflow.backend.exception;

public class DuplicateExceptionRequestException extends RuntimeException {

    public DuplicateExceptionRequestException(Long dealId, ExceptionType exceptionType) {
        super(
                "An active " + exceptionType
                        + " exception already exists for deal "
                        + dealId
                        + "."
        );
    }
}