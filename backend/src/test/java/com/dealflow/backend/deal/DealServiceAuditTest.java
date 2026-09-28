package com.dealflow.backend.deal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dealflow.backend.approval.ApprovalRequestService;
import com.dealflow.backend.audit.AuditEventType;
import com.dealflow.backend.audit.AuditService;
import com.dealflow.backend.cpq.CpqService;
import com.dealflow.backend.exception.DealExceptionService;

@ExtendWith(MockitoExtension.class)
class DealServiceAuditTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private ApprovalRequestService approvalRequestService;

    @Mock
    private DealExceptionService dealExceptionService;

    @Mock
    private AuditService auditService;

    @Mock
    private CpqService cpqService;

    private DealService dealService;

    @BeforeEach
    void setUp() {
        dealService = new DealService(
                dealRepository,
                approvalRequestService,
                dealExceptionService,
                auditService,
                cpqService
        );
    }

    @Test
    void recordsDealCreatedAuditEventAfterSavingDeal() {
        Deal deal = new Deal();
        deal.setId(12L);
        deal.setQuoteNumber("Q-10483");
        deal.setCustomerName("Acme");
        deal.setDealValue(new BigDecimal("100000.00"));
        deal.setDiscountPercentage(new BigDecimal("10.00"));
        deal.setMarginPercentage(new BigDecimal("30.00"));

        when(dealRepository.existsByQuoteNumber("Q-10483")).thenReturn(false);
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> {
            Deal savedDeal = invocation.getArgument(0);
            savedDeal.setId(12L);
            return savedDeal;
        });

        dealService.createDeal(deal);

        verify(auditService).recordUserEvent(
                12L,
                com.dealflow.backend.audit.AuditEntityType.DEAL,
                12L,
                AuditEventType.DEAL_CREATED,
                null,
                "DRAFT",
                "Deal created."
        );
    }
}
