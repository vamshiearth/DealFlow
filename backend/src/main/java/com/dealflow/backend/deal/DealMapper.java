package com.dealflow.backend.deal;

public final class DealMapper {

    private DealMapper() {
    }

    public static DealResponse toResponse(Deal deal) {
        return new DealResponse(
                deal.getId(),
                deal.getQuoteNumber(),
                deal.getCustomerName(),
                deal.getDealValue(),
                deal.getDiscountPercentage(),
                deal.getMarginPercentage(),
                deal.getStatus(),
                deal.getCreatedAt(),
                deal.getUpdatedAt()
        );
    }
}