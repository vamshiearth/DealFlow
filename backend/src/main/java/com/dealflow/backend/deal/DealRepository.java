package com.dealflow.backend.deal;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DealRepository extends JpaRepository<Deal, Long> {

    Optional<Deal> findByQuoteNumber(String quoteNumber);

    boolean existsByQuoteNumber(String quoteNumber);
}