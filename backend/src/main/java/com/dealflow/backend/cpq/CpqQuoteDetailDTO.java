package com.dealflow.backend.cpq;

import java.util.List;

public record CpqQuoteDetailDTO(
        String quoteNumber,
        String customerName,
        String status,
        CpqConfigurationDTO configuration,
        CpqPricingDTO pricing,
        List<CpqQuoteLineDTO> quoteLines,
        List<CpqBomItemDTO> bom
) {
}