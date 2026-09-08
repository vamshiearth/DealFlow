package com.dealflow.backend.deal;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DealService {

    private final DealRepository dealRepository;

    public DealService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
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

        return dealRepository.save(deal);
    }
}