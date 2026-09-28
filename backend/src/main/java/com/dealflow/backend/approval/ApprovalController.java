package com.dealflow.backend.approval;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deals/{dealId}/approvals")
public class ApprovalController {

    private final ApprovalRequestService approvalRequestService;

    public ApprovalController(ApprovalRequestService approvalRequestService) {
        this.approvalRequestService = approvalRequestService;
    }

    @GetMapping
    public List<ApprovalResponse> getApprovalsForDeal(@PathVariable Long dealId) {
        return approvalRequestService
                .getApprovalsForDeal(dealId)
                .stream()
                .map(ApprovalMapper::toResponse)
                .toList();
    }
}