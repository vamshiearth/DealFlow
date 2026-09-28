package com.dealflow.backend.audit;

public final class AuditEventMapper {

    private AuditEventMapper() {
    }

    public static AuditEventResponse toResponse(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getDealId(),
                event.getEntityType(),
                event.getEntityId(),
                event.getEventType(),
                event.getActorType(),
                event.getActorUserId(),
                event.getOldStatus(),
                event.getNewStatus(),
                event.getDescription(),
                event.getCreatedAt()
        );
    }
}
