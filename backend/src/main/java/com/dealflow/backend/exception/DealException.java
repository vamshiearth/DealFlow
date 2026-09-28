package com.dealflow.backend.exception;

import java.math.BigDecimal;
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
@Table(name = "exceptions")
@Getter
@Setter
@NoArgsConstructor
public class DealException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    @Enumerated(EnumType.STRING)
    @Column(name = "exception_type", nullable = false, length = 50)
    private ExceptionType exceptionType;

    @Column(name = "requested_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal requestedValue;

    @Column(name = "standard_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal standardValue;

    @Column(name = "justification", nullable = false, length = 2000)
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ExceptionStatus status = ExceptionStatus.PENDING;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = ExceptionStatus.PENDING;
        }

        createdAt = LocalDateTime.now();
    }
}