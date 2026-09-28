package com.dealflow.backend.cpq;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "cpq.enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class MockCpqClient implements CpqClient {

    private static final Map<String, CpqQuoteResponse> QUOTES = Map.of(
            "Q-10001", quote("Q-10001", "Acme Manufacturing", "100000.00", "8.00", "25.00"),
            "Q-10002", quote("Q-10002", "Northstar Retail", "250000.00", "15.00", "20.00"),
            "Q-10003", quote("Q-10003", "Summit Healthcare", "350000.00", "25.00", "18.00"),
            "Q-10004", quote("Q-10004", "Nova Logistics", "750000.00", "32.00", "12.00"),
            "Q-10005", quote("Q-10005", "GreenBridge Logistics", "520000.00", "27.00", "13.00"),
            "Q-10482", quote("Q-10482", "Acme Corporation", "520000.00", "25.00", "13.00")
    );

            private static final Map<String, CpqQuoteDetailResponse> QUOTE_DETAILS = Map.of(
                "Q-10005", greenBridgeQuote()
            );

    @Override
    public CpqQuoteResponse getQuote(String quoteNumber) {
        validateQuoteNumber(quoteNumber);
        simulateFailure(quoteNumber);
        if (quoteNumber.equalsIgnoreCase("Q-ERROR-BAD-RESPONSE")) {
            return new CpqQuoteResponse(quoteNumber, null, null, null, null, "OPEN");
        }

        return findQuote(quoteNumber);
    }

    @Override
    public CpqQuoteDetailResponse getQuoteDetails(String quoteNumber) {
        validateQuoteNumber(quoteNumber);
        simulateFailure(quoteNumber);
        if (quoteNumber.equalsIgnoreCase("Q-ERROR-BAD-RESPONSE")) {
            return new CpqQuoteDetailResponse(quoteNumber, "GreenBridge Logistics", "OPEN", null, null, null, null);
        }

        CpqQuoteDetailResponse details = QUOTE_DETAILS.get(quoteNumber);
        if (details == null) {
            throw new CpqQuoteNotFoundException(quoteNumber);
        }
        return details;
    }

    private static void simulateFailure(String quoteNumber) {
        switch (quoteNumber.toUpperCase()) {
            case "Q-ERROR-AUTH", "Q-ERROR-401" -> throw new CpqAuthenticationException();
            case "Q-ERROR-TIMEOUT", "Q-TIMEOUT" -> throw new CpqTimeoutException();
            case "Q-ERROR-DOWN", "Q-ERROR-500" -> throw new CpqServiceUnavailableException();
            default -> {
            }
        }
    }

    private static void validateQuoteNumber(String quoteNumber) {
        if (quoteNumber == null || quoteNumber.isBlank()) {
            throw new CpqException("Quote number is required.");
        }
    }

    private static CpqQuoteResponse findQuote(String quoteNumber) {
        CpqQuoteResponse quote = QUOTES.get(quoteNumber);
        if (quote == null) {
            throw new CpqQuoteNotFoundException(quoteNumber);
        }
        return quote;
    }

    private static CpqQuoteResponse quote(
            String quoteNumber,
            String customerName,
            String totalPrice,
            String discountPercentage,
            String marginPercentage
    ) {
        return new CpqQuoteResponse(
                quoteNumber,
                customerName,
                new BigDecimal(totalPrice),
                new BigDecimal(discountPercentage),
                new BigDecimal(marginPercentage),
                "OPEN"
        );
    }

            private static CpqQuoteDetailResponse greenBridgeQuote() {
            return new CpqQuoteDetailResponse(
                "Q-10005",
                "GreenBridge Logistics",
                "OPEN",
                new CpqConfigurationDTO(
                    "Warehouse Automation",
                    "Enterprise Automation Suite",
                    Map.of(
                        "distributionCenters", "5",
                        "sensorPackage", "Premium",
                        "supportLevel", "Enterprise",
                        "supportTerm", "36 Months"
                    )
                ),
                new CpqPricingDTO(
                    new BigDecimal("712328.77"),
                    new BigDecimal("27.00"),
                    new BigDecimal("192328.77"),
                    new BigDecimal("520000.00"),
                    new BigDecimal("13.00")
                ),
                List.of(
                    new CpqQuoteLineDTO(1, "CTRL-ENT-100", "Enterprise Controller", new BigDecimal("5"), new BigDecimal("52000.00"), new BigDecimal("40000.00"), new BigDecimal("200000.00")),
                    new CpqQuoteLineDTO(2, "SENSOR-PRM-50", "Premium Sensor", new BigDecimal("50"), new BigDecimal("4000.00"), new BigDecimal("3000.00"), new BigDecimal("150000.00")),
                    new CpqQuoteLineDTO(3, "SW-ENT-001", "Enterprise Automation Software", BigDecimal.ONE, new BigDecimal("135000.00"), new BigDecimal("100000.00"), new BigDecimal("100000.00")),
                    new CpqQuoteLineDTO(4, "SUP-36-ENT", "Three-Year Enterprise Support", BigDecimal.ONE, new BigDecimal("117328.77"), new BigDecimal("70000.00"), new BigDecimal("70000.00"))
                ),
                List.of(
                    new CpqBomItemDTO("1", null, "CTRL-ENT-100", "Enterprise Controller", new BigDecimal("5")),
                    new CpqBomItemDTO("1.1", "1", "NET-GW-200", "Industrial Network Gateway", new BigDecimal("5")),
                    new CpqBomItemDTO("1.2", "1", "PWR-MOD-50", "Industrial Power Module", new BigDecimal("10")),
                    new CpqBomItemDTO("2", null, "SENSOR-PRM-50", "Premium Sensor", new BigDecimal("50"))
                )
            );
            }
}
