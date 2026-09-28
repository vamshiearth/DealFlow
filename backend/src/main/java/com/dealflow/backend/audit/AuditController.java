package com.dealflow.backend.audit;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deals/{dealId}/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public List<AuditEventResponse> getAuditTrail(@PathVariable Long dealId) {
        return auditService.getAuditTrail(dealId)
                .stream()
                .map(AuditEventMapper::toResponse)
                .toList();
    }
}
