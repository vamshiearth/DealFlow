package com.dealflow.backend.approval;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dealflow.backend.auth.CurrentUserService;
import com.dealflow.backend.audit.AuditEntityType;
import com.dealflow.backend.audit.AuditEventType;
import com.dealflow.backend.audit.AuditService;
import com.dealflow.backend.deal.Deal;
import com.dealflow.backend.deal.DealNotFoundException;
import com.dealflow.backend.deal.DealRepository;
import com.dealflow.backend.deal.DealStatus;

@Service
public class ApprovalRequestService {

    private final ApprovalRuleEngine approvalRuleEngine;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final DealRepository dealRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public ApprovalRequestService(
            ApprovalRuleEngine approvalRuleEngine,
            ApprovalRequestRepository approvalRequestRepository,
            DealRepository dealRepository,
            CurrentUserService currentUserService,
            AuditService auditService) {
        this.approvalRuleEngine = approvalRuleEngine;
        this.approvalRequestRepository = approvalRequestRepository;
        this.dealRepository = dealRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public List<ApprovalRequest> createApprovalRequests(Deal deal) {
        List<ApprovalRole> requiredRoles = approvalRuleEngine.evaluate(deal);
        List<ApprovalRequest> requests = new ArrayList<>();
        int sequence = 1;
        int approvalCycle = nextApprovalCycle(deal.getId());

        for (ApprovalRole role : requiredRoles) {
            ApprovalRequest request = new ApprovalRequest();
            request.setDeal(deal);
            request.setApproverRole(role);
            request.setStatus(ApprovalStatus.PENDING);
            request.setSequence(sequence++);
            request.setApprovalCycle(approvalCycle);
            requests.add(request);
        }

        List<ApprovalRequest> savedRequests = approvalRequestRepository.saveAll(requests);

        for (ApprovalRequest request : savedRequests) {
            auditService.recordSystemEvent(
                deal.getId(),
                AuditEntityType.APPROVAL,
                request.getId(),
                AuditEventType.APPROVAL_CREATED,
                null,
                request.getStatus().name(),
                "Approval request created for "
                    + request.getApproverRole()
                    + " at sequence "
                    + request.getSequence()
                    + ", cycle "
                    + request.getApprovalCycle()
                    + "."
            );
        }

        updateInitialDealStatus(deal, savedRequests);
        return savedRequests;
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequest> getApprovalsForDeal(Long dealId) {
        if (!dealRepository.existsById(dealId)) {
            throw new DealNotFoundException(dealId);
        }

        return approvalRequestRepository
                .findByDealIdOrderByApprovalCycleDescSequenceAsc(dealId);
    }

            @Transactional(readOnly = true)
            public List<ApprovalRequest> getActivePendingApprovals(ApprovalRole role) {
            List<ApprovalRequest> pending = approvalRequestRepository
                .findByStatusOrderByRequestedAtAsc(ApprovalStatus.PENDING);

            return pending.stream()
                .filter(this::isDealActionable)
                .filter(this::isCurrentApprovalCycle)
                .filter(this::isNextRequiredApproval)
                .filter(approval -> role == null || approval.getApproverRole() == role)
                .toList();
            }

            private boolean isDealActionable(ApprovalRequest approval) {
            DealStatus status = approval.getDeal().getStatus();
            return status != DealStatus.APPROVED
                && status != DealStatus.REJECTED
                && status != DealStatus.CHANGES_REQUESTED;
            }

            private boolean isCurrentApprovalCycle(ApprovalRequest approval) {
            List<ApprovalRequest> approvals = approvalRequestRepository
                .findByDealIdOrderByApprovalCycleDescSequenceAsc(approval.getDeal().getId());

            return !approvals.isEmpty()
                && approvals.get(0).getApprovalCycle().equals(approval.getApprovalCycle());
            }

            private boolean isNextRequiredApproval(ApprovalRequest approval) {
            List<ApprovalRequest> approvals = approvalRequestRepository
                .findByDealIdOrderByApprovalCycleDescSequenceAsc(approval.getDeal().getId());

            return approvals.stream()
                .filter(request -> request.getApprovalCycle().equals(approval.getApprovalCycle()))
                .filter(request -> request.getStatus() == ApprovalStatus.PENDING)
                .findFirst()
                .map(next -> next.getId().equals(approval.getId()))
                .orElse(false);
            }

    private int nextApprovalCycle(Long dealId) {
        List<ApprovalRequest> existing = approvalRequestRepository
                .findByDealIdOrderByApprovalCycleDescSequenceAsc(dealId);

        if (existing.isEmpty()) {
            return 1;
        }

        return existing.get(0).getApprovalCycle() + 1;
    }

    @Transactional
    public void supersedePendingApprovals(Long dealId) {
        List<ApprovalRequest> approvals = approvalRequestRepository
                .findByDealIdOrderByApprovalCycleDescSequenceAsc(dealId);

        if (approvals.isEmpty()) {
            return;
        }

        Integer currentCycle = approvals.get(0).getApprovalCycle();
        List<ApprovalRequest> superseded = approvals.stream()
                .filter(approval -> approval.getApprovalCycle().equals(currentCycle))
                .filter(approval -> approval.getStatus() == ApprovalStatus.PENDING)
                .peek(approval -> {
                    approval.setStatus(ApprovalStatus.SUPERSEDED);
                    approval.setResolvedAt(LocalDateTime.now());
                    approval.setComments("Superseded by deal resubmission.");
                })
                .toList();

        if (superseded.isEmpty()) {
            return;
        }

        approvalRequestRepository.saveAll(superseded);

        for (ApprovalRequest approval : superseded) {
            auditService.recordSystemEvent(
                    approval.getDeal().getId(),
                    AuditEntityType.APPROVAL,
                    approval.getId(),
                    AuditEventType.APPROVAL_SUPERSEDED,
                    ApprovalStatus.PENDING.name(),
                    ApprovalStatus.SUPERSEDED.name(),
                    "Approval request superseded by deal resubmission."
            );
        }
    }

    private void updateInitialDealStatus(Deal deal, List<ApprovalRequest> approvals) {
        DealStatus oldStatus = deal.getStatus();

        if (approvals.isEmpty()) {
            deal.setStatus(DealStatus.APPROVED);
        } else {
            deal.setStatus(reviewStatusFor(approvals.get(0).getApproverRole()));
        }

        dealRepository.save(deal);

        if (oldStatus != deal.getStatus()) {
            auditService.recordSystemEvent(
                    deal.getId(),
                    AuditEntityType.DEAL,
                    deal.getId(),
                    AuditEventType.DEAL_WORKFLOW_TRANSITION,
                    oldStatus.name(),
                    deal.getStatus().name(),
                    buildInitialWorkflowDescription(approvals, deal.getStatus())
            );
        }
    }

    private String buildInitialWorkflowDescription(
            List<ApprovalRequest> approvals,
            DealStatus newStatus) {
        if (approvals.isEmpty()) {
            return "Deal automatically approved because no approvals were required.";
        }

        return "Deal entered " + newStatus + " based on the generated approval chain.";
    }

    @Transactional
    public ApprovalRequest approve(Long approvalId, String comments) {
        ApprovalRequest approval = approvalRequestRepository.findById(approvalId)
                .orElseThrow(() -> new ApprovalNotFoundException(approvalId));

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException(
                    "Approval request " + approvalId + " is already " + approval.getStatus() + "."
            );
        }

        validateApproverRole(approval);

        Deal deal = approval.getDeal();
        validateDealIsActionable(deal);

        validateApprovalSequence(approval);

        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setResolvedAt(LocalDateTime.now());
        approval.setResolvedBy(currentUserService.getCurrentUserId());
        approval.setComments(comments);

        ApprovalRequest savedApproval = approvalRequestRepository.save(approval);
        auditService.recordUserEvent(
            savedApproval.getDeal().getId(),
            AuditEntityType.APPROVAL,
            savedApproval.getId(),
            AuditEventType.APPROVAL_APPROVED,
            ApprovalStatus.PENDING.name(),
            ApprovalStatus.APPROVED.name(),
            buildApprovalDecisionDescription(savedApproval, comments)
        );
        updateDealAfterApproval(savedApproval);
        return savedApproval;
    }

    @Transactional
    public ApprovalRequest reject(Long approvalId, String comments) {
        ApprovalRequest approval = approvalRequestRepository.findById(approvalId)
            .orElseThrow(() -> new ApprovalNotFoundException(approvalId));

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException(
                "Approval request " + approvalId + " is already " + approval.getStatus() + "."
            );
        }

        validateApproverRole(approval);
        validateApprovalSequence(approval);

        Deal deal = approval.getDeal();
        validateDealIsActionable(deal);

        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setResolvedAt(LocalDateTime.now());
        approval.setResolvedBy(currentUserService.getCurrentUserId());
        approval.setComments(comments);

        ApprovalRequest savedApproval = approvalRequestRepository.save(approval);
        auditService.recordUserEvent(
            savedApproval.getDeal().getId(),
            AuditEntityType.APPROVAL,
            savedApproval.getId(),
            AuditEventType.APPROVAL_REJECTED,
            ApprovalStatus.PENDING.name(),
            ApprovalStatus.REJECTED.name(),
            buildApprovalDecisionDescription(savedApproval, comments)
        );

        DealStatus oldDealStatus = deal.getStatus();
        deal.setStatus(DealStatus.REJECTED);
        dealRepository.save(deal);
        recordWorkflowTransition(
            deal,
            oldDealStatus,
            DealStatus.REJECTED,
            "Deal rejected after approval request " + savedApproval.getId() + " was rejected."
        );
        return savedApproval;
    }

    @Transactional
    public ApprovalRequest requestChanges(Long approvalId, String comments) {
        ApprovalRequest approval = approvalRequestRepository.findById(approvalId)
                .orElseThrow(() -> new ApprovalNotFoundException(approvalId));

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException(
                    "Approval request " + approvalId + " is already " + approval.getStatus() + "."
            );
        }

        validateApproverRole(approval);
        validateApprovalSequence(approval);

        Deal deal = approval.getDeal();
        validateDealIsActionable(deal);

        approval.setStatus(ApprovalStatus.CHANGES_REQUESTED);
        approval.setResolvedAt(LocalDateTime.now());
        approval.setResolvedBy(currentUserService.getCurrentUserId());
        approval.setComments(comments);

        ApprovalRequest savedApproval = approvalRequestRepository.save(approval);
        auditService.recordUserEvent(
                savedApproval.getDeal().getId(),
                AuditEntityType.APPROVAL,
                savedApproval.getId(),
                AuditEventType.APPROVAL_CHANGES_REQUESTED,
                ApprovalStatus.PENDING.name(),
                ApprovalStatus.CHANGES_REQUESTED.name(),
                buildApprovalDecisionDescription(savedApproval, comments)
        );

        DealStatus oldDealStatus = deal.getStatus();
        deal.setStatus(DealStatus.CHANGES_REQUESTED);
        dealRepository.save(deal);
        recordWorkflowTransition(
                deal,
                oldDealStatus,
                DealStatus.CHANGES_REQUESTED,
                "Deal moved to CHANGES_REQUESTED after approval request "
                        + savedApproval.getId()
                        + "."
        );
        return savedApproval;
    }

    private String buildApprovalDecisionDescription(
            ApprovalRequest approval,
            String comments) {
        String description = approval.getApproverRole() + " approval completed.";

        if (comments != null && !comments.isBlank()) {
            description += " Comment: " + comments;
        }

        return description;
    }

    private void validateApproverRole(ApprovalRequest approval) {
        ApprovalRole requiredRole = approval.getApproverRole();
        boolean allowed = currentUserService.hasRole(requiredRole.name());
        boolean adminOverride = currentUserService.hasRole("CPQ_ADMIN");

        if (!allowed && !adminOverride) {
            throw new ForbiddenApprovalActionException(approval.getId(), requiredRole);
        }
    }

    private void validateDealIsActionable(Deal deal) {
        if (deal.getStatus() == DealStatus.REJECTED
                || deal.getStatus() == DealStatus.APPROVED
                || deal.getStatus() == DealStatus.CHANGES_REQUESTED) {
            throw new IllegalStateException(
                    "Deal " + deal.getId()
                            + " cannot accept approval decisions while in status "
                            + deal.getStatus() + "."
            );
        }
    }

        private void validateApprovalSequence(ApprovalRequest approval) {
        List<ApprovalRequest> approvals = approvalRequestRepository
            .findByDealIdOrderByApprovalCycleDescSequenceAsc(approval.getDeal().getId());

        ApprovalRequest nextRequiredApproval = approvals.stream()
            .filter(request -> request.getStatus() == ApprovalStatus.PENDING)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "No pending approvals remain for deal "
                    + approval.getDeal().getId()
                    + "."
            ));

        if (!nextRequiredApproval.getId().equals(approval.getId())) {
            throw new ApprovalOutOfSequenceException(
                approval.getId(),
                nextRequiredApproval.getSequence()
            );
        }
        }

    private void updateDealAfterApproval(ApprovalRequest completedApproval) {
        Deal deal = completedApproval.getDeal();
        DealStatus oldStatus = deal.getStatus();
        List<ApprovalRequest> approvals = approvalRequestRepository
            .findByDealIdOrderByApprovalCycleDescSequenceAsc(deal.getId());

        ApprovalRequest nextPending = approvals.stream()
                .filter(approval -> approval.getStatus() == ApprovalStatus.PENDING)
                .findFirst()
                .orElse(null);

        deal.setStatus(nextPending == null
                ? DealStatus.APPROVED
                : reviewStatusFor(nextPending.getApproverRole()));
        dealRepository.save(deal);

        recordWorkflowTransition(
            deal,
            oldStatus,
            deal.getStatus(),
            "Deal workflow advanced after approval " + completedApproval.getId() + "."
        );
        }

        private void recordWorkflowTransition(
            Deal deal,
            DealStatus oldStatus,
            DealStatus newStatus,
            String description) {
        if (oldStatus != newStatus) {
            auditService.recordSystemEvent(
                deal.getId(),
                AuditEntityType.DEAL,
                deal.getId(),
                AuditEventType.DEAL_WORKFLOW_TRANSITION,
                oldStatus.name(),
                newStatus.name(),
                description
            );
        }
    }

    private DealStatus reviewStatusFor(ApprovalRole role) {
        return switch (role) {
            case SALES_MANAGER -> DealStatus.MANAGER_REVIEW;
            case FINANCE -> DealStatus.FINANCE_REVIEW;
            case VP_SALES -> DealStatus.VP_REVIEW;
        };
    }
}