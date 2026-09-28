package com.dealflow.backend.audit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    List<AuditEvent> findByDealIdOrderByCreatedAtAsc(Long dealId);
}
