package com.dealflow.backend.cpq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CpqServiceTest {

    @Test
    void returnsNormalizedQuoteFromClientResponse() {
        CpqClient client = mock(CpqClient.class);
        when(client.getQuote("Q-10482")).thenReturn(new CpqQuoteResponse(
                "Q-10482",
                "Acme Corporation",
                new BigDecimal("520000.00"),
                new BigDecimal("25.00"),
                new BigDecimal("13.00"),
                "OPEN"
        ));

        CpqQuoteDTO result = new CpqService(client).getQuote("Q-10482");

        assertEquals("Q-10482", result.quoteNumber());
        assertEquals(new BigDecimal("520000.00"), result.totalPrice());
    }

    @Test
    void rejectsNullClientResponse() {
        CpqClient client = mock(CpqClient.class);
        when(client.getQuote("Q-10482")).thenReturn(null);

        assertThrows(CpqException.class, () -> new CpqService(client).getQuote("Q-10482"));
    }

    @Test
    void propagatesClientFailure() {
        CpqClient client = mock(CpqClient.class);
        CpqException failure = new CpqException("CPQ unavailable.");
        when(client.getQuote("Q-10482")).thenThrow(failure);

        CpqException result = assertThrows(
                CpqException.class,
                () -> new CpqService(client).getQuote("Q-10482")
        );

        assertEquals(failure, result);
    }

    @Test
    void returnsNormalizedQuoteDetails() {
        CpqClient client = new MockCpqClient();

        CpqQuoteDetailDTO result = new CpqService(client).getQuoteDetails("Q-10005");

        assertEquals("Q-10005", result.quoteNumber());
        assertEquals("GreenBridge Logistics", result.customerName());
    }

    @Test
    void rejectsNullDetailClientResponse() {
        CpqClient client = mock(CpqClient.class);
        when(client.getQuoteDetails("Q-10005")).thenReturn(null);

        assertThrows(CpqException.class, () -> new CpqService(client).getQuoteDetails("Q-10005"));
    }
}
