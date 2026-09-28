package com.dealflow.backend.audit;

import java.time.LocalDateTime;

public record AuditEventResponse(
        Long id,
        Long dealId,
        AuditEntityType entityType,
        Long entityId,
        AuditEventType eventType,
        AuditActorType actorType,
        Long actorUserId,
        String oldStatus,
        String newStatus,
        String description,
        LocalDateTime createdAt
) {
}
