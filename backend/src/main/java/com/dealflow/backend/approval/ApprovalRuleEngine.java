package com.dealflow.backend.approval;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.dealflow.backend.deal.Deal;

@Service
public class ApprovalRuleEngine {

    private static final BigDecimal TEN_PERCENT = new BigDecimal("10.00");
    private static final BigDecimal TWENTY_PERCENT = new BigDecimal("20.00");
    private static final BigDecimal THIRTY_PERCENT = new BigDecimal("30.00");
    private static final BigDecimal MINIMUM_MARGIN = new BigDecimal("15.00");
    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("500000.00");

    public List<ApprovalRole> evaluate(Deal deal) {
        Set<ApprovalRole> requiredApprovals = new LinkedHashSet<>();

        evaluateDiscount(deal.getDiscountPercentage(), requiredApprovals);
        evaluateMargin(deal.getMarginPercentage(), requiredApprovals);
        evaluateDealValue(deal.getDealValue(), requiredApprovals);

        return new ArrayList<>(requiredApprovals);
    }

    private void evaluateDiscount(BigDecimal discount, Set<ApprovalRole> approvals) {
        if (discount.compareTo(TEN_PERCENT) < 0) {
            return;
        }

        approvals.add(ApprovalRole.SALES_MANAGER);

        if (discount.compareTo(TWENTY_PERCENT) > 0) {
            approvals.add(ApprovalRole.FINANCE);
        }

        if (discount.compareTo(THIRTY_PERCENT) > 0) {
            approvals.add(ApprovalRole.VP_SALES);
        }
    }

    private void evaluateMargin(BigDecimal margin, Set<ApprovalRole> approvals) {
        if (margin.compareTo(MINIMUM_MARGIN) < 0) {
            approvals.add(ApprovalRole.FINANCE);
        }
    }

    private void evaluateDealValue(BigDecimal dealValue, Set<ApprovalRole> approvals) {
        if (dealValue.compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            approvals.add(ApprovalRole.VP_SALES);
        }
    }
}