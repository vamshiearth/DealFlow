package com.dealflow.backend.exception;

public final class ExceptionMapper {

    private ExceptionMapper() {
    }

    public static ExceptionResponse toResponse(DealException exception) {
        return new ExceptionResponse(
                exception.getId(),
                exception.getDeal().getId(),
                exception.getExceptionType(),
                exception.getRequestedValue(),
                exception.getStandardValue(),
                exception.getJustification(),
                exception.getStatus(),
                exception.getCreatedBy(),
                exception.getResolvedBy(),
                exception.getCreatedAt(),
                exception.getResolvedAt()
        );
    }
}