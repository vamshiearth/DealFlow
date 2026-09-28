package com.dealflow.backend.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dealflow.backend.auth.CurrentUserService;
import com.dealflow.backend.deal.DealNotFoundException;
import com.dealflow.backend.deal.DealRepository;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private DealRepository dealRepository;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(
                auditEventRepository,
                currentUserService,
                dealRepository
        );
    }

    @Test
    void recordsUserEventWithAuthenticatedActor() {
        when(currentUserService.getCurrentUserId()).thenReturn(7L);
        when(auditEventRepository.save(any(AuditEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuditEvent result = auditService.recordUserEvent(
                12L,
                AuditEntityType.DEAL,
                12L,
                AuditEventType.DEAL_CREATED,
                null,
                "DRAFT",
                "Deal created."
        );

        assertEquals(AuditActorType.USER, result.getActorType());
        assertEquals(7L, result.getActorUserId());
        assertEquals(AuditEventType.DEAL_CREATED, result.getEventType());
        verify(auditEventRepository).save(result);
    }

    @Test
    void recordsSystemEventWithoutUserActor() {
        when(auditEventRepository.save(any(AuditEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuditEvent result = auditService.recordSystemEvent(
                12L,
                AuditEntityType.DEAL,
                12L,
                AuditEventType.DEAL_UPDATED,
                "DRAFT",
                "DRAFT",
                "System event."
        );

        assertEquals(AuditActorType.SYSTEM, result.getActorType());
        assertEquals(null, result.getActorUserId());
    }

    @Test
    void returnsAuditTrailForExistingDeal() {
        AuditEvent event = new AuditEvent();
        when(dealRepository.existsById(12L)).thenReturn(true);
        when(auditEventRepository.findByDealIdOrderByCreatedAtAsc(12L))
                .thenReturn(List.of(event));

        assertEquals(List.of(event), auditService.getAuditTrail(12L));
    }

    @Test
    void rejectsAuditTrailForUnknownDeal() {
        when(dealRepository.existsById(99L)).thenReturn(false);

        assertThrows(DealNotFoundException.class, () -> auditService.getAuditTrail(99L));
    }
}
