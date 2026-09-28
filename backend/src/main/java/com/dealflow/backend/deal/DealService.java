package com.dealflow.backend.deal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dealflow.backend.approval.ApprovalRequestService;
import com.dealflow.backend.audit.AuditEntityType;
import com.dealflow.backend.audit.AuditEventType;
import com.dealflow.backend.audit.AuditService;
import com.dealflow.backend.cpq.CpqQuoteDTO;
import com.dealflow.backend.cpq.CpqService;
import com.dealflow.backend.exception.DealExceptionService;

@Service
public class DealService {

    private final DealRepository dealRepository;
    private final ApprovalRequestService approvalRequestService;
    private final DealExceptionService dealExceptionService;
    private final AuditService auditService;
    private final CpqService cpqService;

    public DealService(
            DealRepository dealRepository,
            ApprovalRequestService approvalRequestService,
            DealExceptionService dealExceptionService,
            AuditService auditService,
            CpqService cpqService) {
        this.dealRepository = dealRepository;
        this.approvalRequestService = approvalRequestService;
        this.dealExceptionService = dealExceptionService;
        this.auditService = auditService;
        this.cpqService = cpqService;
    }

    @Transactional(readOnly = true)
    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Deal> getDealById(Long id) {
        return dealRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Deal getRequiredDealById(Long id) {
        return dealRepository.findById(id)
                .orElseThrow(() -> new DealNotFoundException(id));
    }

    @Transactional
    public Deal submitDeal(Long id) {
        Deal deal = getRequiredDealById(id);

        if (deal.getStatus() != DealStatus.DRAFT) {
            throw new InvalidDealTransitionException(
                    deal.getStatus(),
                    DealStatus.SUBMITTED
            );
        }

        dealExceptionService.validateExceptionsForSubmission(deal);

        DealStatus oldStatus = deal.getStatus();
        deal.setStatus(DealStatus.SUBMITTED);

        Deal submittedDeal = dealRepository.save(deal);

        auditService.recordUserEvent(
            submittedDeal.getId(),
            AuditEntityType.DEAL,
            submittedDeal.getId(),
            AuditEventType.DEAL_SUBMITTED,
            oldStatus.name(),
            submittedDeal.getStatus().name(),
            "Deal submitted for approval."
        );

        approvalRequestService.createApprovalRequests(submittedDeal);

        return submittedDeal;
    }

    @Transactional
    public Deal updateDeal(
            Long id,
            String customerName,
            BigDecimal dealValue,
            BigDecimal discountPercentage,
            BigDecimal marginPercentage) {
        Deal deal = getRequiredDealById(id);

        if (deal.getStatus() != DealStatus.DRAFT
                && deal.getStatus() != DealStatus.CHANGES_REQUESTED) {
            throw new IllegalStateException(
                    "Deal " + id + " cannot be edited while in status " + deal.getStatus() + "."
            );
        }

        String oldCustomerName = deal.getCustomerName();
        BigDecimal oldDealValue = deal.getDealValue();
        BigDecimal oldDiscount = deal.getDiscountPercentage();
        BigDecimal oldMargin = deal.getMarginPercentage();
        DealStatus currentStatus = deal.getStatus();

        deal.setCustomerName(customerName);
        deal.setDealValue(dealValue);
        deal.setDiscountPercentage(discountPercentage);
        deal.setMarginPercentage(marginPercentage);

        Deal updatedDeal = dealRepository.save(deal);

    auditService.recordUserEvent(
        updatedDeal.getId(),
        AuditEntityType.DEAL,
        updatedDeal.getId(),
        AuditEventType.DEAL_UPDATED,
        currentStatus.name(),
        updatedDeal.getStatus().name(),
        buildDealUpdateDescription(
            oldCustomerName,
            oldDealValue,
            oldDiscount,
            oldMargin,
            updatedDeal
        )
    );

        dealExceptionService.supersedeStalePendingExceptions(updatedDeal);
        return updatedDeal;
    }

    @Transactional
    public Deal resubmitDeal(Long id) {
        Deal deal = getRequiredDealById(id);

        if (deal.getStatus() != DealStatus.CHANGES_REQUESTED) {
            throw new InvalidDealTransitionException(deal.getStatus(), DealStatus.SUBMITTED);
        }

        dealExceptionService.validateExceptionsForSubmission(deal);

        DealStatus oldStatus = deal.getStatus();
        approvalRequestService.supersedePendingApprovals(id);
        deal.setStatus(DealStatus.SUBMITTED);
        Deal submittedDeal = dealRepository.save(deal);

        auditService.recordUserEvent(
            submittedDeal.getId(),
            AuditEntityType.DEAL,
            submittedDeal.getId(),
            AuditEventType.DEAL_RESUBMITTED,
            oldStatus.name(),
            submittedDeal.getStatus().name(),
            "Deal resubmitted after requested changes."
        );

        approvalRequestService.createApprovalRequests(submittedDeal);

        return submittedDeal;
    }

    private String buildDealUpdateDescription(
            String oldCustomerName,
            BigDecimal oldDealValue,
            BigDecimal oldDiscount,
            BigDecimal oldMargin,
            Deal updatedDeal) {
        List<String> changes = new ArrayList<>();

        if (!oldCustomerName.equals(updatedDeal.getCustomerName())) {
            changes.add("customerName: " + oldCustomerName + " -> " + updatedDeal.getCustomerName());
        }
        if (oldDealValue.compareTo(updatedDeal.getDealValue()) != 0) {
            changes.add("dealValue: " + oldDealValue + " -> " + updatedDeal.getDealValue());
        }
        if (oldDiscount.compareTo(updatedDeal.getDiscountPercentage()) != 0) {
            changes.add("discountPercentage: " + oldDiscount + " -> " + updatedDeal.getDiscountPercentage());
        }
        if (oldMargin.compareTo(updatedDeal.getMarginPercentage()) != 0) {
            changes.add("marginPercentage: " + oldMargin + " -> " + updatedDeal.getMarginPercentage());
        }

        if (changes.isEmpty()) {
            return "Deal update submitted with no value changes.";
        }

        return "Deal updated: " + String.join(", ", changes);
    }

    @Transactional(readOnly = true)
    public Optional<Deal> getDealByQuoteNumber(String quoteNumber) {
        return dealRepository.findByQuoteNumber(quoteNumber);
    }

    @Transactional
    public Deal createDeal(Deal deal) {
        if (dealRepository.existsByQuoteNumber(deal.getQuoteNumber())) {
            throw new IllegalArgumentException(
                    "A deal with quote number "
                            + deal.getQuoteNumber()
                            + " already exists."
            );
        }

        deal.setId(null);
        deal.setStatus(DealStatus.DRAFT);

        Deal savedDeal = dealRepository.save(deal);

        auditService.recordUserEvent(
            savedDeal.getId(),
            AuditEntityType.DEAL,
            savedDeal.getId(),
            AuditEventType.DEAL_CREATED,
            null,
            savedDeal.getStatus().name(),
            "Deal created."
        );

        return savedDeal;
    }

    @Transactional
    public Deal importFromCpq(String quoteNumber) {
        CpqQuoteDTO quote = cpqService.getQuote(quoteNumber);

        Deal deal = new Deal();
        deal.setQuoteNumber(quote.quoteNumber());
        deal.setCustomerName(quote.customerName());
        deal.setDealValue(quote.totalPrice());
        deal.setDiscountPercentage(quote.discount());
        deal.setMarginPercentage(quote.margin());

        return createDeal(deal);
    }
}