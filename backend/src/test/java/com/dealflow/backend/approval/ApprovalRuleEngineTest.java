package com.dealflow.backend.approval;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.dealflow.backend.deal.Deal;

class ApprovalRuleEngineTest {

    private final ApprovalRuleEngine approvalRuleEngine = new ApprovalRuleEngine();

    @Test
    void shouldRequireNoApprovalForLowDiscountHealthyMarginAndLowValue() {
        assertEquals(List.of(), approvalRuleEngine.evaluate(createDeal("5.00", "25.00", "100000.00")));
    }

    @Test
    void shouldRequireManagerForFifteenPercentDiscount() {
        assertEquals(List.of(ApprovalRole.SALES_MANAGER),
                approvalRuleEngine.evaluate(createDeal("15.00", "25.00", "100000.00")));
    }

    @Test
    void shouldRequireManagerAndFinanceForTwentyFivePercentDiscount() {
        assertEquals(List.of(ApprovalRole.SALES_MANAGER, ApprovalRole.FINANCE),
                approvalRuleEngine.evaluate(createDeal("25.00", "20.00", "100000.00")));
    }

    @Test
    void shouldRequireFinanceForLowMargin() {
        assertEquals(List.of(ApprovalRole.FINANCE),
                approvalRuleEngine.evaluate(createDeal("5.00", "13.00", "100000.00")));
    }

    @Test
    void shouldRequireVpForHighValueDeal() {
        assertEquals(List.of(ApprovalRole.VP_SALES),
                approvalRuleEngine.evaluate(createDeal("5.00", "25.00", "520000.00")));
    }

    @Test
    void shouldGenerateFullApprovalChainForAcmeScenario() {
        assertEquals(List.of(ApprovalRole.SALES_MANAGER, ApprovalRole.FINANCE, ApprovalRole.VP_SALES),
                approvalRuleEngine.evaluate(createDeal("25.00", "13.00", "520000.00")));
    }

    private Deal createDeal(String discount, String margin, String dealValue) {
        Deal deal = new Deal();
        deal.setDiscountPercentage(new BigDecimal(discount));
        deal.setMarginPercentage(new BigDecimal(margin));
        deal.setDealValue(new BigDecimal(dealValue));
        return deal;
    }
}