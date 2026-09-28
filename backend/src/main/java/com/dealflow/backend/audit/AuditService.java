package com.dealflow.backend.audit;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dealflow.backend.auth.CurrentUserService;
import com.dealflow.backend.deal.DealNotFoundException;
import com.dealflow.backend.deal.DealRepository;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final CurrentUserService currentUserService;
    private final DealRepository dealRepository;

    public AuditService(
            AuditEventRepository auditEventRepository,
            CurrentUserService currentUserService,
            DealRepository dealRepository) {
        this.auditEventRepository = auditEventRepository;
        this.currentUserService = currentUserService;
        this.dealRepository = dealRepository;
    }

    @Transactional
    public AuditEvent recordUserEvent(
            Long dealId,
            AuditEntityType entityType,
            Long entityId,
            AuditEventType eventType,
            String oldStatus,
            String newStatus,
            String description) {
        AuditEvent event = createEvent(
                dealId,
                entityType,
                entityId,
                eventType,
                AuditActorType.USER,
                currentUserService.getCurrentUserId(),
                oldStatus,
                newStatus,
                description
        );

        return auditEventRepository.save(event);
    }

    @Transactional
    public AuditEvent recordSystemEvent(
            Long dealId,
            AuditEntityType entityType,
            Long entityId,
            AuditEventType eventType,
            String oldStatus,
            String newStatus,
            String description) {
        AuditEvent event = createEvent(
                dealId,
                entityType,
                entityId,
                eventType,
                AuditActorType.SYSTEM,
                null,
                oldStatus,
                newStatus,
                description
        );

        return auditEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<AuditEvent> getAuditTrail(Long dealId) {
        if (!dealRepository.existsById(dealId)) {
            throw new DealNotFoundException(dealId);
        }

        return auditEventRepository.findByDealIdOrderByCreatedAtAsc(dealId);
    }

    private AuditEvent createEvent(
            Long dealId,
            AuditEntityType entityType,
            Long entityId,
            AuditEventType eventType,
            AuditActorType actorType,
            Long actorUserId,
            String oldStatus,
            String newStatus,
            String description) {
        AuditEvent event = new AuditEvent();
        event.setDealId(dealId);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setEventType(eventType);
        event.setActorType(actorType);
        event.setActorUserId(actorUserId);
        event.setOldStatus(oldStatus);
        event.setNewStatus(newStatus);
        event.setDescription(description);
        return event;
    }
}
