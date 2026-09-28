package com.dealflow.backend.deal;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deals")
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @GetMapping
    public List<DealResponse> getAllDeals() {
        return dealService.getAllDeals()
                .stream()
                .map(DealMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public DealResponse getDealById(@PathVariable Long id) {
        Deal deal = dealService.getRequiredDealById(id);
        return DealMapper.toResponse(deal);
    }

    @PreAuthorize("hasAnyRole('SALES_REP', 'CPQ_ADMIN')")
    @PutMapping("/{id}")
    public DealResponse updateDeal(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDealRequest request) {
        Deal updatedDeal = dealService.updateDeal(
                id,
                request.customerName(),
                request.dealValue(),
                request.discountPercentage(),
                request.marginPercentage()
        );
        return DealMapper.toResponse(updatedDeal);
    }

    @PreAuthorize("hasAnyRole('SALES_REP', 'CPQ_ADMIN')")
    @PostMapping("/{id}/submit")
    public DealResponse submitDeal(@PathVariable Long id) {
        Deal submittedDeal = dealService.submitDeal(id);
        return DealMapper.toResponse(submittedDeal);
    }

    @PreAuthorize("hasAnyRole('SALES_REP', 'CPQ_ADMIN')")
    @PostMapping("/{id}/resubmit")
    public DealResponse resubmitDeal(@PathVariable Long id) {
        return DealMapper.toResponse(dealService.resubmitDeal(id));
    }

    @PreAuthorize("hasAnyRole('SALES_REP', 'CPQ_ADMIN')")
    @PostMapping
    public ResponseEntity<DealResponse> createDeal(@Valid @RequestBody CreateDealRequest request) {
        Deal deal = new Deal();
        deal.setQuoteNumber(request.quoteNumber());
        deal.setCustomerName(request.customerName());
        deal.setDealValue(request.dealValue());
        deal.setDiscountPercentage(request.discountPercentage());
        deal.setMarginPercentage(request.marginPercentage());

        Deal createdDeal = dealService.createDeal(deal);
        DealResponse response = DealMapper.toResponse(createdDeal);

        return ResponseEntity
                .status(HttpStatus.CREATED)
            .body(response);
    }

        @PreAuthorize("hasAnyRole('SALES_REP', 'CPQ_ADMIN')")
        @PostMapping("/import-from-cpq/{quoteNumber}")
        public ResponseEntity<DealResponse> importFromCpq(@PathVariable String quoteNumber) {
            Deal importedDeal = dealService.importFromCpq(quoteNumber);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(DealMapper.toResponse(importedDeal));
        }
}