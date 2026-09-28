package com.dealflow.backend.approval;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.dealflow.backend.deal.Deal;

@Entity
@Table(name = "approval_requests")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_role", nullable = false, length = 50)
    private ApprovalRole approverRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "approval_cycle", nullable = false)
    private Integer approvalCycle;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "comments", length = 2000)
    private String comments;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = ApprovalStatus.PENDING;
        }

        requestedAt = LocalDateTime.now();
    }
}