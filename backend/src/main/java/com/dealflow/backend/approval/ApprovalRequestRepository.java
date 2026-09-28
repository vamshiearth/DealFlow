package com.dealflow.backend.approval;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {

    List<ApprovalRequest> findByDealIdOrderBySequenceAsc(Long dealId);

    List<ApprovalRequest> findByDealIdOrderByApprovalCycleDescSequenceAsc(Long dealId);

    boolean existsByDealId(Long dealId);

    @EntityGraph(attributePaths = "deal")
    List<ApprovalRequest> findByStatusOrderByRequestedAtAsc(ApprovalStatus status);
}