package com.dealflow.backend.exception;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
public class DealExceptionService {

    private static final BigDecimal STANDARD_DISCOUNT_LIMIT = new BigDecimal("20.00");

    private final DealExceptionRepository exceptionRepository;
    private final DealRepository dealRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public DealExceptionService(
            DealExceptionRepository exceptionRepository,
            DealRepository dealRepository,
            CurrentUserService currentUserService,
            AuditService auditService) {
        this.exceptionRepository = exceptionRepository;
        this.dealRepository = dealRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public DealException createException(
            Long dealId,
            ExceptionType exceptionType,
            BigDecimal requestedValue,
            BigDecimal standardValue,
            String justification) {
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new DealNotFoundException(dealId));

        validateDealCanRequestException(deal);
        validateExceptionRequest(deal, exceptionType, requestedValue, standardValue);
        validateNoDuplicateException(deal, exceptionType, requestedValue, standardValue);

        DealException exception = new DealException();
        exception.setDeal(deal);
        exception.setExceptionType(exceptionType);
        exception.setRequestedValue(requestedValue);
        exception.setStandardValue(standardValue);
        exception.setJustification(justification);
        exception.setStatus(ExceptionStatus.PENDING);
        exception.setCreatedBy(currentUserService.getCurrentUserId());

        DealException savedException = exceptionRepository.save(exception);

        auditService.recordUserEvent(
            savedException.getDeal().getId(),
            AuditEntityType.EXCEPTION,
            savedException.getId(),
            AuditEventType.EXCEPTION_CREATED,
            null,
            ExceptionStatus.PENDING.name(),
            "Created "
                + savedException.getExceptionType()
                + " exception. Requested value: "
                + savedException.getRequestedValue()
                + ", standard value: "
                + savedException.getStandardValue()
                + "."
        );

        return savedException;
    }

    private void validateDealCanRequestException(Deal deal) {
        if (deal.getStatus() != DealStatus.DRAFT
                && deal.getStatus() != DealStatus.CHANGES_REQUESTED) {
            throw new InvalidExceptionRequestException(
                    "Exceptions can only be requested while deal "
                            + deal.getId()
                            + " is DRAFT or CHANGES_REQUESTED. Current status: "
                            + deal.getStatus()
                            + "."
            );
        }
    }

    private void validateExceptionRequest(
            Deal deal,
            ExceptionType exceptionType,
            BigDecimal requestedValue,
            BigDecimal standardValue) {
        switch (exceptionType) {
            case DISCOUNT -> validateDiscountExceptionRequest(deal, requestedValue, standardValue);
            case MARGIN -> validateMarginExceptionRequest(deal, requestedValue);
        }
    }

    private void validateDiscountExceptionRequest(
            Deal deal,
            BigDecimal requestedValue,
            BigDecimal standardValue) {
        BigDecimal currentDiscount = deal.getDiscountPercentage();

        if (currentDiscount.compareTo(STANDARD_DISCOUNT_LIMIT) <= 0) {
            throw new InvalidExceptionRequestException(
                    "Deal " + deal.getId() + " does not require a DISCOUNT exception."
            );
        }
        if (requestedValue.compareTo(currentDiscount) != 0) {
            throw new InvalidExceptionRequestException(
                    "Requested discount " + requestedValue
                            + " does not match the deal discount " + currentDiscount + "."
            );
        }
        if (standardValue.compareTo(STANDARD_DISCOUNT_LIMIT) != 0) {
            throw new InvalidExceptionRequestException(
                    "Standard discount value must be " + STANDARD_DISCOUNT_LIMIT + "."
            );
        }
    }

    private void validateMarginExceptionRequest(Deal deal, BigDecimal requestedValue) {
        if (requestedValue.compareTo(deal.getMarginPercentage()) != 0) {
            throw new InvalidExceptionRequestException(
                    "Requested margin " + requestedValue
                            + " does not match the deal margin "
                            + deal.getMarginPercentage()
                            + "."
            );
        }
    }

    private void validateNoDuplicateException(
            Deal deal,
            ExceptionType exceptionType,
            BigDecimal requestedValue,
            BigDecimal standardValue) {
        boolean duplicate = exceptionRepository
                .findByDealIdAndExceptionTypeOrderByCreatedAtDesc(deal.getId(), exceptionType)
                .stream()
                .anyMatch(exception ->
                        exception.getRequestedValue().compareTo(requestedValue) == 0
                                && exception.getStandardValue().compareTo(standardValue) == 0
                                && (exception.getStatus() == ExceptionStatus.PENDING
                                || exception.getStatus() == ExceptionStatus.APPROVED)
                );

        if (duplicate) {
            throw new DuplicateExceptionRequestException(deal.getId(), exceptionType);
        }
    }

    @Transactional(readOnly = true)
    public List<DealException> getExceptionsForDeal(Long dealId) {
        if (!dealRepository.existsById(dealId)) {
            throw new DealNotFoundException(dealId);
        }

        return exceptionRepository.findByDealIdOrderByCreatedAtAsc(dealId);
    }

    @Transactional(readOnly = true)
    public void validateExceptionsForSubmission(Deal deal) {
        evaluateExceptionRequirements(deal).stream()
            .filter(requirement -> !requirement.satisfied())
            .findFirst()
            .ifPresent(requirement -> {
            throw new ExceptionApprovalRequiredException(
                requirement.exceptionType(),
                requirement.requestedValue(),
                requirement.standardValue()
            );
            });
        }

        @Transactional(readOnly = true)
        public List<ExceptionRequirementResponse> getExceptionRequirements(Long dealId) {
        Deal deal = dealRepository.findById(dealId)
            .orElseThrow(() -> new DealNotFoundException(dealId));

        return evaluateExceptionRequirements(deal);
        }

    @Transactional
    public void supersedeStalePendingExceptions(Deal deal) {
        List<DealException> pending = exceptionRepository
                .findByDealIdOrderByCreatedAtAsc(deal.getId())
                .stream()
                .filter(exception -> exception.getStatus() == ExceptionStatus.PENDING)
                .toList();

        List<DealException> superseded = pending.stream()
                .filter(exception -> !isExceptionStillRelevant(exception))
            .peek(exception -> {
                    exception.setStatus(ExceptionStatus.SUPERSEDED);
                    exception.setResolvedAt(LocalDateTime.now());
            })
            .toList();

        if (superseded.isEmpty()) {
            return;
        }

        exceptionRepository.saveAll(superseded);

        for (DealException exception : superseded) {
            auditService.recordSystemEvent(
                exception.getDeal().getId(),
                AuditEntityType.EXCEPTION,
                exception.getId(),
                AuditEventType.EXCEPTION_SUPERSEDED,
                ExceptionStatus.PENDING.name(),
                ExceptionStatus.SUPERSEDED.name(),
                "Exception automatically superseded because the deal values changed."
            );
        }
    }

    @Transactional(readOnly = true)
    public List<DealException> getActivePendingExceptions(ExceptionType type) {
        return exceptionRepository.findByStatusOrderByCreatedAtAsc(ExceptionStatus.PENDING)
                .stream()
                .filter(this::isExceptionQueueEligible)
                .filter(exception -> type == null || exception.getExceptionType() == type)
                .toList();
    }

    private boolean isExceptionQueueEligible(DealException exception) {
        DealStatus status = exception.getDeal().getStatus();
        return status != DealStatus.APPROVED
                && status != DealStatus.REJECTED
                && status != DealStatus.CANCELLED
                && isExceptionStillRelevant(exception);
    }

    private boolean isExceptionStillRelevant(DealException exception) {
        Deal deal = exception.getDeal();

        return switch (exception.getExceptionType()) {
            case DISCOUNT -> exception.getRequestedValue().compareTo(deal.getDiscountPercentage()) == 0
                    && exception.getStandardValue().compareTo(STANDARD_DISCOUNT_LIMIT) == 0
                    && deal.getDiscountPercentage().compareTo(STANDARD_DISCOUNT_LIMIT) > 0;
            case MARGIN -> exception.getRequestedValue().compareTo(deal.getMarginPercentage()) == 0;
        };
    }

        private List<ExceptionRequirementResponse> evaluateExceptionRequirements(Deal deal) {
        List<ExceptionRequirementResponse> requirements = new ArrayList<>();
        addDiscountRequirement(deal, requirements);
        return requirements;
        }

        private void addDiscountRequirement(
            Deal deal,
            List<ExceptionRequirementResponse> requirements) {
        BigDecimal requestedDiscount = deal.getDiscountPercentage();

        if (requestedDiscount.compareTo(STANDARD_DISCOUNT_LIMIT) <= 0) {
            return;
        }

        List<DealException> matchingExceptions = exceptionRepository
            .findByDealIdAndExceptionTypeOrderByCreatedAtDesc(
                deal.getId(),
                ExceptionType.DISCOUNT
            )
            .stream()
            .filter(exception -> exception.getRequestedValue().compareTo(requestedDiscount) == 0)
            .filter(exception -> exception.getStandardValue().compareTo(STANDARD_DISCOUNT_LIMIT) == 0)
            .toList();

        DealException approvedException = matchingExceptions.stream()
            .filter(exception -> exception.getStatus() == ExceptionStatus.APPROVED)
            .findFirst()
            .orElse(null);

        DealException latestMatchingException = matchingExceptions.stream()
            .findFirst()
            .orElse(null);

        requirements.add(new ExceptionRequirementResponse(
            ExceptionType.DISCOUNT,
            requestedDiscount,
            STANDARD_DISCOUNT_LIMIT,
            approvedException != null,
            latestMatchingException == null ? null : latestMatchingException.getId(),
            latestMatchingException == null ? null : latestMatchingException.getStatus()
        ));
    }

    @Transactional
    public DealException approveException(Long exceptionId) {
        DealException exception = exceptionRepository.findById(exceptionId)
                .orElseThrow(() -> new DealExceptionNotFoundException(exceptionId));

        validatePending(exception);
        exception.setStatus(ExceptionStatus.APPROVED);
        exception.setResolvedAt(LocalDateTime.now());
        exception.setResolvedBy(currentUserService.getCurrentUserId());

        DealException savedException = exceptionRepository.save(exception);
        auditService.recordUserEvent(
            savedException.getDeal().getId(),
            AuditEntityType.EXCEPTION,
            savedException.getId(),
            AuditEventType.EXCEPTION_APPROVED,
            ExceptionStatus.PENDING.name(),
            ExceptionStatus.APPROVED.name(),
            "Exception approved."
        );

        return savedException;
    }

    @Transactional
    public DealException rejectException(Long exceptionId) {
        DealException exception = exceptionRepository.findById(exceptionId)
                .orElseThrow(() -> new DealExceptionNotFoundException(exceptionId));

        validatePending(exception);
        exception.setStatus(ExceptionStatus.REJECTED);
        exception.setResolvedAt(LocalDateTime.now());
        exception.setResolvedBy(currentUserService.getCurrentUserId());

        DealException savedException = exceptionRepository.save(exception);
        auditService.recordUserEvent(
            savedException.getDeal().getId(),
            AuditEntityType.EXCEPTION,
            savedException.getId(),
            AuditEventType.EXCEPTION_REJECTED,
            ExceptionStatus.PENDING.name(),
            ExceptionStatus.REJECTED.name(),
            "Exception rejected."
        );

        return savedException;
    }

    private void validatePending(DealException exception) {
        if (exception.getStatus() != ExceptionStatus.PENDING) {
            throw new IllegalStateException(
                    "Deal exception " + exception.getId()
                            + " is already " + exception.getStatus() + "."
            );
        }
    }
}