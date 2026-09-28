package com.dealflow.backend.exception;

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
class ExceptionAuditTest {

    @Mock
    private DealExceptionRepository exceptionRepository;

    @Mock
    private DealRepository dealRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditService auditService;

    private DealExceptionService service;

    @BeforeEach
    void setUp() {
        service = new DealExceptionService(
                exceptionRepository,
                dealRepository,
                currentUserService,
                auditService
        );
    }

    @Test
    void auditsExceptionCreationAsUser() {
        Deal deal = deal(12L, DealStatus.DRAFT, "27.00");
        when(dealRepository.findById(12L)).thenReturn(Optional.of(deal));
        when(exceptionRepository.findByDealIdAndExceptionTypeOrderByCreatedAtDesc(
                12L, ExceptionType.DISCOUNT)).thenReturn(List.of());
        when(currentUserService.getCurrentUserId()).thenReturn(7L);
        when(exceptionRepository.save(any(DealException.class))).thenAnswer(invocation -> {
            DealException exception = invocation.getArgument(0);
            exception.setId(31L);
            return exception;
        });

        service.createException(
                12L,
                ExceptionType.DISCOUNT,
                new BigDecimal("27.00"),
                new BigDecimal("20.00"),
                "Strategic agreement."
        );

        verify(auditService).recordUserEvent(
                12L,
                AuditEntityType.EXCEPTION,
                31L,
                AuditEventType.EXCEPTION_CREATED,
                null,
                "PENDING",
                "Created DISCOUNT exception. Requested value: 27.00, standard value: 20.00."
        );
    }

    @Test
    void auditsExceptionApprovalAndRejectionAsUsers() {
        Deal deal = deal(12L, DealStatus.DRAFT, "27.00");
        DealException exception = exception(deal, 31L);
        when(exceptionRepository.findById(31L)).thenReturn(Optional.of(exception));
        when(currentUserService.getCurrentUserId()).thenReturn(8L);
        when(exceptionRepository.save(exception)).thenReturn(exception);

        service.approveException(31L);

        verify(auditService).recordUserEvent(
                12L,
                AuditEntityType.EXCEPTION,
                31L,
                AuditEventType.EXCEPTION_APPROVED,
                "PENDING",
                "APPROVED",
                "Exception approved."
        );

        exception.setStatus(ExceptionStatus.PENDING);
        service.rejectException(31L);

        verify(auditService).recordUserEvent(
                12L,
                AuditEntityType.EXCEPTION,
                31L,
                AuditEventType.EXCEPTION_REJECTED,
                "PENDING",
                "REJECTED",
                "Exception rejected."
        );
    }

    @Test
    void auditsStaleExceptionSupersedingAsSystem() {
        Deal deal = deal(12L, DealStatus.DRAFT, "18.00");
        DealException exception = exception(deal, 31L);
        when(exceptionRepository.findByDealIdOrderByCreatedAtAsc(12L))
                .thenReturn(List.of(exception));

        service.supersedeStalePendingExceptions(deal);

        verify(auditService).recordSystemEvent(
                12L,
                AuditEntityType.EXCEPTION,
                31L,
                AuditEventType.EXCEPTION_SUPERSEDED,
                "PENDING",
                "SUPERSEDED",
                "Exception automatically superseded because the deal values changed."
        );
    }

    private Deal deal(Long id, DealStatus status, String discount) {
        Deal deal = new Deal();
        deal.setId(id);
        deal.setStatus(status);
        deal.setDiscountPercentage(new BigDecimal(discount));
        deal.setMarginPercentage(new BigDecimal("20.00"));
        return deal;
    }

    private DealException exception(Deal deal, Long id) {
        DealException exception = new DealException();
        exception.setId(id);
        exception.setDeal(deal);
        exception.setExceptionType(ExceptionType.DISCOUNT);
        exception.setRequestedValue(new BigDecimal("27.00"));
        exception.setStandardValue(new BigDecimal("20.00"));
        exception.setJustification("Strategic agreement.");
        exception.setStatus(ExceptionStatus.PENDING);
        return exception;
    }
}
