package com.dealflow.backend.exception;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealExceptionRepository extends JpaRepository<DealException, Long> {

    @EntityGraph(attributePaths = "deal")
    List<DealException> findByDealIdOrderByCreatedAtAsc(Long dealId);

    List<DealException> findByDealIdAndExceptionTypeOrderByCreatedAtDesc(
            Long dealId,
            ExceptionType exceptionType);

    @EntityGraph(attributePaths = "deal")
    List<DealException> findByStatusOrderByCreatedAtAsc(ExceptionStatus status);
}