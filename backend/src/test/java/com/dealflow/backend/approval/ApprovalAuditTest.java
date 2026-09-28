package com.dealflow.backend.approval;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dealflow.backend.auth.CurrentUserService;
import com.dealflow.backend.audit.AuditEntityType;
import com.dealflow.backend.audit.AuditEventType;
import com.dealflow.backend.audit.AuditService;
import com.dealflow.backend.deal.Deal;
import com.dealflow.backend.deal.DealRepository;
import com.dealflow.backend.deal.DealStatus;

@ExtendWith(MockitoExtension.class)
class ApprovalAuditTest {

    @Mock
    private ApprovalRuleEngine approvalRuleEngine;

    @Mock
    private ApprovalRequestRepository approvalRequestRepository;

    @Mock
    private DealRepository dealRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditService auditService;

    private ApprovalRequestService service;

    @BeforeEach
    void setUp() {
        service = new ApprovalRequestService(
                approvalRuleEngine,
                approvalRequestRepository,
                dealRepository,
                currentUserService,
                auditService
        );
    }

    @Test
    void auditsApprovalCreationAndInitialWorkflowTransitionAsSystem() {
        Deal deal = deal(12L, DealStatus.SUBMITTED);
        ApprovalRequest request = pendingApproval(deal, 21L, ApprovalRole.SALES_MANAGER, 1, 1);
        when(approvalRuleEngine.evaluate(deal)).thenReturn(List.of(ApprovalRole.SALES_MANAGER));
        when(approvalRequestRepository.findByDealIdOrderByApprovalCycleDescSequenceAsc(12L))
                .thenReturn(List.of());
        when(approvalRequestRepository.saveAll(any())).thenReturn(List.of(request));

        service.createApprovalRequests(deal);

        verify(auditService).recordSystemEvent(
                12L,
                AuditEntityType.APPROVAL,
                21L,
                AuditEventType.APPROVAL_CREATED,
                null,
                "PENDING",
                "Approval request created for SALES_MANAGER at sequence 1, cycle 1."
        );
        verify(auditService).recordSystemEvent(
                12L,
                AuditEntityType.DEAL,
                12L,
                AuditEventType.DEAL_WORKFLOW_TRANSITION,
                "SUBMITTED",
                "MANAGER_REVIEW",
                "Deal entered MANAGER_REVIEW based on the generated approval chain."
        );
    }

    @Test
    void auditsApprovalDecisionAsUserAndDealTransitionAsSystem() {
        Deal deal = deal(12L, DealStatus.MANAGER_REVIEW);
        ApprovalRequest approval = pendingApproval(deal, 21L, ApprovalRole.SALES_MANAGER, 1, 1);
        when(approvalRequestRepository.findById(21L)).thenReturn(Optional.of(approval));
        when(currentUserService.hasRole("SALES_MANAGER")).thenReturn(true);
        when(currentUserService.hasRole("CPQ_ADMIN")).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(7L);
        when(approvalRequestRepository.findByDealIdOrderByApprovalCycleDescSequenceAsc(12L))
                .thenReturn(List.of(approval));
        when(approvalRequestRepository.save(approval)).thenReturn(approval);

        service.approve(21L, "Pricing approved.");

        verify(auditService).recordUserEvent(
                12L,
                AuditEntityType.APPROVAL,
                21L,
                AuditEventType.APPROVAL_APPROVED,
                "PENDING",
                "APPROVED",
                "SALES_MANAGER approval completed. Comment: Pricing approved."
        );
        verify(auditService).recordSystemEvent(
                12L,
                AuditEntityType.DEAL,
                12L,
                AuditEventType.DEAL_WORKFLOW_TRANSITION,
                "MANAGER_REVIEW",
                "APPROVED",
                "Deal workflow advanced after approval 21."
        );
    }

    @Test
    void auditsSupersededApprovalsAsSystem() {
        Deal deal = deal(12L, DealStatus.CHANGES_REQUESTED);
        ApprovalRequest approval = pendingApproval(deal, 21L, ApprovalRole.FINANCE, 1, 1);
        when(approvalRequestRepository.findByDealIdOrderByApprovalCycleDescSequenceAsc(12L))
                .thenReturn(List.of(approval));

        service.supersedePendingApprovals(12L);

        verify(auditService).recordSystemEvent(
                12L,
                AuditEntityType.APPROVAL,
                21L,
                AuditEventType.APPROVAL_SUPERSEDED,
                "PENDING",
                "SUPERSEDED",
                "Approval request superseded by deal resubmission."
        );
    }

    private Deal deal(Long id, DealStatus status) {
        Deal deal = new Deal();
        deal.setId(id);
        deal.setStatus(status);
        deal.setDealValue(new BigDecimal("100000.00"));
        deal.setDiscountPercentage(new BigDecimal("25.00"));
        deal.setMarginPercentage(new BigDecimal("20.00"));
        return deal;
    }

    private ApprovalRequest pendingApproval(
            Deal deal,
            Long id,
            ApprovalRole role,
            int sequence,
            int cycle) {
        ApprovalRequest approval = new ApprovalRequest();
        approval.setId(id);
        approval.setDeal(deal);
        approval.setApproverRole(role);
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setSequence(sequence);
        approval.setApprovalCycle(cycle);
        return approval;
    }
}
