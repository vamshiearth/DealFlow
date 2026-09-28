package com.dealflow.backend.approval;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.dealflow.backend.auth.CurrentUserService;
import com.dealflow.backend.audit.AuditService;
import com.dealflow.backend.deal.Deal;
import com.dealflow.backend.deal.DealRepository;
import com.dealflow.backend.deal.DealStatus;

class ApprovalRoleAuthorizationTest {

    @Test
    void shouldRejectFinanceUserApprovingSalesManagerApproval() {
        ApprovalRequestRepository approvalRequestRepository = mock(ApprovalRequestRepository.class);
        DealRepository dealRepository = mock(DealRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        AuditService auditService = mock(AuditService.class);

        when(currentUserService.hasRole("SALES_MANAGER")).thenReturn(false);
        when(currentUserService.hasRole("CPQ_ADMIN")).thenReturn(false);

        ApprovalRequestService service = new ApprovalRequestService(
                new ApprovalRuleEngine(),
                approvalRequestRepository,
                dealRepository,
                currentUserService,
                auditService
        );

        Deal deal = new Deal();
        deal.setId(12L);
        deal.setStatus(DealStatus.MANAGER_REVIEW);

        ApprovalRequest approval = new ApprovalRequest();
        approval.setId(99L);
        approval.setDeal(deal);
        approval.setApproverRole(ApprovalRole.SALES_MANAGER);
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setSequence(1);
        approval.setApprovalCycle(1);

        when(approvalRequestRepository.findById(99L)).thenReturn(Optional.of(approval));
        when(approvalRequestRepository.findByDealIdOrderByApprovalCycleDescSequenceAsc(12L))
                .thenReturn(List.of(approval));

        assertThrows(ForbiddenApprovalActionException.class,
                () -> service.approve(99L, "not allowed"));
    }
}
