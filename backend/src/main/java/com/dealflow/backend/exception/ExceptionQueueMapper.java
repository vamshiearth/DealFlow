package com.dealflow.backend.exception;

public final class ExceptionQueueMapper {

    private ExceptionQueueMapper() {
    }

    public static ExceptionQueueResponse toResponse(DealException exception) {
        return new ExceptionQueueResponse(
                exception.getId(),
                exception.getDeal().getId(),
                exception.getDeal().getQuoteNumber(),
                exception.getDeal().getCustomerName(),
                exception.getDeal().getDealValue(),
                exception.getDeal().getDiscountPercentage(),
                exception.getDeal().getMarginPercentage(),
                exception.getDeal().getStatus(),
                exception.getExceptionType(),
                exception.getRequestedValue(),
                exception.getStandardValue(),
                exception.getJustification(),
                exception.getStatus(),
                exception.getCreatedAt()
        );
    }
}