package com.dealflow.backend.cpq;

public final class CpqQuoteMapper {

    private CpqQuoteMapper() {
    }

    public static CpqQuoteDTO toDto(CpqQuoteResponse response) {
        return new CpqQuoteDTO(
                response.quoteNumber(),
                response.customerName(),
                response.totalPrice(),
                response.discountPercentage(),
                response.marginPercentage(),
                response.transactionStatus()
        );
    }

    public static CpqQuoteDetailDTO toDetailDto(CpqQuoteDetailResponse response) {
        return new CpqQuoteDetailDTO(
                response.quoteNumber(),
                response.customerName(),
                response.transactionStatus(),
                response.configuration(),
                response.pricing(),
                response.quoteLines(),
                response.bom()
        );
    }
}
