package com.dealflow.backend.cpq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class MockCpqClientTest {

    private final MockCpqClient client = new MockCpqClient();

    @Test
    void returnsMockQuote() {
        CpqQuoteResponse result = client.getQuote("Q-10005");

        assertEquals("Q-10005", result.quoteNumber());
        assertEquals("GreenBridge Logistics", result.customerName());
        assertEquals(new BigDecimal("520000.00"), result.totalPrice());
        assertEquals(new BigDecimal("27.00"), result.discountPercentage());
        assertEquals(new BigDecimal("13.00"), result.marginPercentage());
        assertEquals("OPEN", result.transactionStatus());
    }

    @Test
    void returnsDifferentScenariosFromSandboxDataset() {
        CpqQuoteResponse normal = client.getQuote("Q-10001");
        CpqQuoteResponse executiveApproval = client.getQuote("Q-10004");

        assertEquals(new BigDecimal("8.00"), normal.discountPercentage());
        assertEquals(new BigDecimal("32.00"), executiveApproval.discountPercentage());
        assertEquals(new BigDecimal("750000.00"), executiveApproval.totalPrice());
    }

    @Test
    void simulatesKnownCpqFailures() {
        assertThrows(CpqException.class, () -> client.getQuote("Q-ERROR-401"));
        assertThrows(CpqException.class, () -> client.getQuote("Q-ERROR-404"));
        assertThrows(CpqException.class, () -> client.getQuote("Q-ERROR-500"));
        assertThrows(CpqException.class, () -> client.getQuote("Q-TIMEOUT"));
        assertThrows(CpqException.class, () -> client.getQuote("Q-UNKNOWN"));
    }

    @Test
    void returnsDetailedQuoteWithConsistentPricingAndBom() {
        CpqQuoteDetailResponse result = client.getQuoteDetails("Q-10005");

        assertEquals("GreenBridge Logistics", result.customerName());
        assertEquals("Warehouse Automation", result.configuration().productFamily());
        assertEquals("5", result.configuration().attributes().get("distributionCenters"));
        assertEquals(new BigDecimal("520000.00"), result.pricing().netPrice());
        assertEquals(new BigDecimal("27.00"), result.pricing().discountPercentage());
        assertEquals(4, result.quoteLines().size());
        assertEquals(4, result.bom().size());

        BigDecimal lineTotal = result.quoteLines().stream()
                .map(CpqQuoteLineDTO::extendedNetPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(result.pricing().netPrice(), lineTotal);
    }

    @Test
    void rejectsUnknownDetailedQuote() {
        assertThrows(CpqException.class, () -> client.getQuoteDetails("Q-UNKNOWN"));
        assertThrows(CpqException.class, () -> client.getQuoteDetails(" "));
    }

    @Test
    void rejectsBlankQuoteNumber() {
        assertThrows(CpqException.class, () -> client.getQuote(" "));
    }
}
