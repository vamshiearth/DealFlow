package com.dealflow.backend.cpq;

import java.util.List;

public record CpqQuoteDetailResponse(
        String quoteNumber,
        String customerName,
        String transactionStatus,
        CpqConfigurationDTO configuration,
        CpqPricingDTO pricing,
        List<CpqQuoteLineDTO> quoteLines,
        List<CpqBomItemDTO> bom
) {
}