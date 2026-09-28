package com.dealflow.backend.approval;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalDecisionController {

    private final ApprovalRequestService approvalRequestService;

    public ApprovalDecisionController(ApprovalRequestService approvalRequestService) {
        this.approvalRequestService = approvalRequestService;
    }

    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'FINANCE', 'VP_SALES', 'CPQ_ADMIN')")
    @PostMapping("/{id}/approve")
    public ApprovalResponse approve(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalDecisionRequest request) {
        String comments = request == null ? null : request.comments();
        ApprovalRequest approval = approvalRequestService.approve(id, comments);
        return ApprovalMapper.toResponse(approval);
    }

    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'FINANCE', 'VP_SALES', 'CPQ_ADMIN')")
    @PostMapping("/{id}/reject")
    public ApprovalResponse reject(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalDecisionRequest request) {
        String comments = request == null ? null : request.comments();
        ApprovalRequest approval = approvalRequestService.reject(id, comments);
        return ApprovalMapper.toResponse(approval);
    }

    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'FINANCE', 'VP_SALES', 'CPQ_ADMIN')")
    @PostMapping("/{id}/request-changes")
    public ApprovalResponse requestChanges(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalDecisionRequest request) {
        String comments = request == null ? null : request.comments();
        ApprovalRequest approval = approvalRequestService.requestChanges(id, comments);
        return ApprovalMapper.toResponse(approval);
    }

    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'FINANCE', 'VP_SALES', 'CPQ_ADMIN')")
    @GetMapping("/pending")
    public List<ApprovalQueueResponse> getPendingApprovals(
            @RequestParam(required = false) ApprovalRole role) {
        return approvalRequestService
                .getActivePendingApprovals(role)
                .stream()
                .map(ApprovalQueueMapper::toResponse)
                .toList();
    }
}