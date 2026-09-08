package com.dealflow.backend.deal;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public List<Deal> getAllDeals() {
        return dealService.getAllDeals();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Deal> getDealById(@PathVariable Long id) {
        return dealService.getDealById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Deal> createDeal(@Valid @RequestBody CreateDealRequest request) {
        Deal deal = new Deal();
        deal.setQuoteNumber(request.quoteNumber());
        deal.setCustomerName(request.customerName());
        deal.setDealValue(request.dealValue());
        deal.setDiscountPercentage(request.discountPercentage());
        deal.setMarginPercentage(request.marginPercentage());

        Deal createdDeal = dealService.createDeal(deal);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdDeal);
    }
}