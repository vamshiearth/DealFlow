package com.dealflow.backend.deal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dealflow.backend.approval.ApprovalRequestService;
import com.dealflow.backend.audit.AuditEntityType;
import com.dealflow.backend.audit.AuditEventType;
import com.dealflow.backend.audit.AuditService;
import com.dealflow.backend.cpq.CpqService;
import com.dealflow.backend.exception.DealExceptionService;
import com.dealflow.backend.exception.ExceptionApprovalRequiredException;
import com.dealflow.backend.exception.ExceptionType;

@ExtendWith(MockitoExtension.class)
class DealServiceLifecycleAuditTest {

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
    void auditsDealUpdateWithChangedValues() {
        Deal deal = deal(12L, DealStatus.DRAFT);
        when(dealRepository.findById(12L)).thenReturn(java.util.Optional.of(deal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dealService.updateDeal(
                12L,
                "New Customer",
                new BigDecimal("120000.00"),
                new BigDecimal("18.00"),
                new BigDecimal("32.00")
        );

        ArgumentCaptor<String> description = ArgumentCaptor.forClass(String.class);
        verify(auditService).recordUserEvent(
            eq(12L),
            eq(AuditEntityType.DEAL),
            eq(12L),
            eq(AuditEventType.DEAL_UPDATED),
            eq("DRAFT"),
            eq("DRAFT"),
                description.capture()
        );
        org.junit.jupiter.api.Assertions.assertTrue(description.getValue().contains("discountPercentage: 25.00 -> 18.00"));
        org.junit.jupiter.api.Assertions.assertTrue(description.getValue().contains("dealValue: 100000.00 -> 120000.00"));
        verify(dealExceptionService).supersedeStalePendingExceptions(deal);
    }

    @Test
    void auditsSubmissionBeforeApprovalGeneration() {
        Deal deal = deal(12L, DealStatus.DRAFT);
        when(dealRepository.findById(12L)).thenReturn(java.util.Optional.of(deal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dealService.submitDeal(12L);

        verify(auditService).recordUserEvent(
                12L,
                AuditEntityType.DEAL,
                12L,
                AuditEventType.DEAL_SUBMITTED,
                "DRAFT",
                "SUBMITTED",
                "Deal submitted for approval."
        );
        verify(approvalRequestService).createApprovalRequests(deal);
    }

    @Test
    void auditsResubmissionFromChangesRequested() {
        Deal deal = deal(12L, DealStatus.CHANGES_REQUESTED);
        when(dealRepository.findById(12L)).thenReturn(java.util.Optional.of(deal));
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dealService.resubmitDeal(12L);

        verify(auditService).recordUserEvent(
                12L,
                AuditEntityType.DEAL,
                12L,
                AuditEventType.DEAL_RESUBMITTED,
                "CHANGES_REQUESTED",
                "SUBMITTED",
                "Deal resubmitted after requested changes."
        );
        verify(approvalRequestService).supersedePendingApprovals(12L);
        verify(approvalRequestService).createApprovalRequests(deal);
    }

    @Test
    void doesNotAuditInvalidSubmission() {
        Deal deal = deal(12L, DealStatus.APPROVED);
        when(dealRepository.findById(12L)).thenReturn(java.util.Optional.of(deal));

        assertThrows(InvalidDealTransitionException.class, () -> dealService.submitDeal(12L));

        verify(auditService, never()).recordUserEvent(
            anyLong(), any(AuditEntityType.class), anyLong(),
            any(AuditEventType.class), any(), any(), any()
        );
    }

    @Test
    void doesNotAuditSubmissionBlockedByExceptionPolicy() {
        Deal deal = deal(12L, DealStatus.DRAFT);
        when(dealRepository.findById(12L)).thenReturn(java.util.Optional.of(deal));
        org.mockito.Mockito.doThrow(new ExceptionApprovalRequiredException(
                ExceptionType.DISCOUNT,
                new BigDecimal("25.00"),
                new BigDecimal("20.00")
        )).when(dealExceptionService).validateExceptionsForSubmission(deal);

        assertThrows(ExceptionApprovalRequiredException.class, () -> dealService.submitDeal(12L));

        verify(auditService, never()).recordUserEvent(
            anyLong(), any(AuditEntityType.class), anyLong(),
            any(AuditEventType.class), any(), any(), any()
        );
    }

    @Test
    void doesNotAuditInvalidResubmission() {
        Deal deal = deal(12L, DealStatus.DRAFT);
        when(dealRepository.findById(12L)).thenReturn(java.util.Optional.of(deal));

        assertThrows(InvalidDealTransitionException.class, () -> dealService.resubmitDeal(12L));

        verify(auditService, never()).recordUserEvent(
            anyLong(), any(AuditEntityType.class), anyLong(),
            any(AuditEventType.class), any(), any(), any()
        );
    }

    private Deal deal(Long id, DealStatus status) {
        Deal deal = new Deal();
        deal.setId(id);
        deal.setStatus(status);
        deal.setCustomerName("Original Customer");
        deal.setDealValue(new BigDecimal("100000.00"));
        deal.setDiscountPercentage(new BigDecimal("25.00"));
        deal.setMarginPercentage(new BigDecimal("20.00"));
        return deal;
    }
}
