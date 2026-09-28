package com.dealflow.backend.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dealflow.backend.auth.CurrentUserService;
import com.dealflow.backend.audit.AuditService;
import com.dealflow.backend.deal.Deal;
import com.dealflow.backend.deal.DealStatus;

@ExtendWith(MockitoExtension.class)
class DealExceptionServiceTest {

    @Mock
    private DealExceptionRepository exceptionRepository;

    @Mock
    private com.dealflow.backend.deal.DealRepository dealRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditService auditService;

    private DealExceptionService exceptionService;

    @BeforeEach
    void setUp() {
        exceptionService = new DealExceptionService(
            exceptionRepository,
            dealRepository,
            currentUserService,
            auditService
        );
        lenient().when(currentUserService.getCurrentUserId()).thenReturn(7L);
    }

    @Test
    void approvesPendingExceptionAndSetsResolvedAt() {
        DealException exception = pendingException(1L);
        when(exceptionRepository.findById(1L)).thenReturn(Optional.of(exception));
        when(exceptionRepository.save(any(DealException.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DealException result = exceptionService.approveException(1L);

        assertEquals(ExceptionStatus.APPROVED, result.getStatus());
        assertNotNull(result.getResolvedAt());
        assertEquals(7L, result.getResolvedBy());
        verify(exceptionRepository).save(exception);
    }

    @Test
    void rejectsPendingExceptionAndSetsResolvedAt() {
        DealException exception = pendingException(2L);
        when(exceptionRepository.findById(2L)).thenReturn(Optional.of(exception));
        when(exceptionRepository.save(any(DealException.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DealException result = exceptionService.rejectException(2L);

        assertEquals(ExceptionStatus.REJECTED, result.getStatus());
        assertNotNull(result.getResolvedAt());
        assertEquals(7L, result.getResolvedBy());
    }

    @Test
    void rejectsSecondDecisionOnApprovedException() {
        DealException exception = pendingException(3L);
        exception.setStatus(ExceptionStatus.APPROVED);
        when(exceptionRepository.findById(3L)).thenReturn(Optional.of(exception));

        assertThrows(IllegalStateException.class, () -> exceptionService.rejectException(3L));
    }

    @Test
    void rejectsUnknownExceptionId() {
        when(exceptionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(DealExceptionNotFoundException.class, () -> exceptionService.approveException(99L));
    }

        @Test
        void recordsAuthenticatedUserWhenCreatingException() {
        Deal deal = dealWithDiscount("27.00");
        deal.setStatus(DealStatus.DRAFT);
        when(dealRepository.findById(10L)).thenReturn(Optional.of(deal));
        when(exceptionRepository.findByDealIdAndExceptionTypeOrderByCreatedAtDesc(
            10L, ExceptionType.DISCOUNT)).thenReturn(List.of());
        when(exceptionRepository.save(any(DealException.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        DealException result = exceptionService.createException(
            10L,
            ExceptionType.DISCOUNT,
            new BigDecimal("27.00"),
            new BigDecimal("20.00"),
            "Strategic agreement."
        );

        assertEquals(7L, result.getCreatedBy());
        }

    @Test
    void allowsSubmissionWhenDiscountIsAtOrBelowStandardLimit() {
        Deal deal = dealWithDiscount("20.00");

        exceptionService.validateExceptionsForSubmission(deal);
        when(dealRepository.findById(10L)).thenReturn(Optional.of(deal));
        assertEquals(List.of(), exceptionService.getExceptionRequirements(10L));
    }

    @Test
    void blocksSubmissionWhenHighDiscountHasNoException() {
        Deal deal = dealWithDiscount("27.00");
        when(exceptionRepository.findByDealIdAndExceptionTypeOrderByCreatedAtDesc(
                10L, ExceptionType.DISCOUNT)).thenReturn(List.of());

        assertThrows(
                ExceptionApprovalRequiredException.class,
                () -> exceptionService.validateExceptionsForSubmission(deal)
        );
        when(dealRepository.findById(10L)).thenReturn(Optional.of(deal));
        ExceptionRequirementResponse requirement = exceptionService
            .getExceptionRequirements(10L)
            .getFirst();
        assertEquals(false, requirement.satisfied());
        assertEquals(null, requirement.exceptionId());
    }

    @Test
    void blocksSubmissionWhenExceptionIsPendingOrRejected() {
        Deal deal = dealWithDiscount("27.00");
        DealException pending = pendingException(11L);
        DealException rejected = pendingException(12L);
        rejected.setStatus(ExceptionStatus.REJECTED);
        when(exceptionRepository.findByDealIdAndExceptionTypeOrderByCreatedAtDesc(
                10L, ExceptionType.DISCOUNT)).thenReturn(List.of(pending, rejected));

        assertThrows(
                ExceptionApprovalRequiredException.class,
                () -> exceptionService.validateExceptionsForSubmission(deal)
        );
    }

    @Test
    void allowsSubmissionWithMatchingApprovedException() {
        Deal deal = dealWithDiscount("27.00");
        DealException approved = pendingException(13L);
        approved.setStatus(ExceptionStatus.APPROVED);
        when(exceptionRepository.findByDealIdAndExceptionTypeOrderByCreatedAtDesc(
                10L, ExceptionType.DISCOUNT)).thenReturn(List.of(approved));

        exceptionService.validateExceptionsForSubmission(deal);
        when(dealRepository.findById(10L)).thenReturn(Optional.of(deal));
        assertEquals(true, exceptionService.getExceptionRequirements(10L).getFirst().satisfied());
    }

    @Test
    void blocksSubmissionWhenApprovedExceptionHasDifferentRequestedValue() {
        Deal deal = dealWithDiscount("27.00");
        DealException approved = pendingException(14L);
        approved.setStatus(ExceptionStatus.APPROVED);
        approved.setRequestedValue(new BigDecimal("25.00"));
        when(exceptionRepository.findByDealIdAndExceptionTypeOrderByCreatedAtDesc(
                10L, ExceptionType.DISCOUNT)).thenReturn(List.of(approved));

        assertThrows(
                ExceptionApprovalRequiredException.class,
                () -> exceptionService.validateExceptionsForSubmission(deal)
        );
    }

    @Test
    void includesRelevantPendingDiscountExceptionInQueue() {
        Deal deal = dealWithDiscount("27.00");
        deal.setStatus(DealStatus.DRAFT);
        DealException exception = pendingException(15L);
        exception.setDeal(deal);
        when(exceptionRepository.findByStatusOrderByCreatedAtAsc(ExceptionStatus.PENDING))
                .thenReturn(List.of(exception));

        assertEquals(List.of(exception), exceptionService.getActivePendingExceptions(null));
    }

    @Test
    void excludesPendingExceptionForRejectedDeal() {
        Deal deal = dealWithDiscount("27.00");
        deal.setStatus(DealStatus.REJECTED);
        DealException exception = pendingException(16L);
        exception.setDeal(deal);
        when(exceptionRepository.findByStatusOrderByCreatedAtAsc(ExceptionStatus.PENDING))
                .thenReturn(List.of(exception));

        assertEquals(List.of(), exceptionService.getActivePendingExceptions(null));
    }

    @Test
    void excludesStalePendingDiscountException() {
        Deal deal = dealWithDiscount("18.00");
        deal.setStatus(DealStatus.DRAFT);
        DealException exception = pendingException(17L);
        exception.setDeal(deal);
        when(exceptionRepository.findByStatusOrderByCreatedAtAsc(ExceptionStatus.PENDING))
                .thenReturn(List.of(exception));

        assertEquals(List.of(), exceptionService.getActivePendingExceptions(ExceptionType.DISCOUNT));
    }

    private Deal dealWithDiscount(String discount) {
        Deal deal = new Deal();
        deal.setId(10L);
        deal.setDiscountPercentage(new BigDecimal(discount));
        return deal;
    }

    private DealException pendingException(Long id) {
        DealException exception = new DealException();
        exception.setId(id);
        Deal deal = new Deal();
        deal.setId(10L);
        exception.setDeal(deal);
        exception.setExceptionType(ExceptionType.DISCOUNT);
        exception.setRequestedValue(new BigDecimal("27.00"));
        exception.setStandardValue(new BigDecimal("20.00"));
        exception.setJustification("Three-year contract.");
        exception.setStatus(ExceptionStatus.PENDING);
        exception.setCreatedAt(LocalDateTime.now());
        return exception;
    }
}