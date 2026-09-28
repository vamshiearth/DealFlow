package com.dealflow.backend.cpq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CpqQuoteMapperTest {

    @Test
    void mapsRawCpqResponseToNormalizedDto() {
        CpqQuoteResponse response = new CpqQuoteResponse(
                "Q-10482",
                "Acme Corporation",
                new BigDecimal("520000.00"),
                new BigDecimal("25.00"),
                new BigDecimal("13.00"),
                "OPEN"
        );

        CpqQuoteDTO dto = CpqQuoteMapper.toDto(response);

        assertEquals("Q-10482", dto.quoteNumber());
        assertEquals("Acme Corporation", dto.customerName());
        assertEquals(new BigDecimal("520000.00"), dto.totalPrice());
        assertEquals(new BigDecimal("25.00"), dto.discount());
        assertEquals(new BigDecimal("13.00"), dto.margin());
        assertEquals("OPEN", dto.status());
    }
}
