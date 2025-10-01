// com.tss.aml.entity.Transaction.java

package com.tss.aml.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency; // e.g., USD, EUR

    @Column(name = "receiver_country_code", length = 2)
    private String receiverCountryCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false)
    private String status; // "PENDING", "APPROVED", "BLOCKED", "FLAGGED"

    @Column(name = "nlp_score")
    private Integer nlpScore;

    @Column(name = "rule_engine_score")
    private Integer ruleEngineScore;

    @Column(name = "combined_risk_score")
    private Integer combinedRiskScore;

    @Column(name = "threshold_exceeded", columnDefinition = "boolean default false")
    private boolean thresholdExceeded;

    @Column(name = "alert_id")
    private String alertId; // optional link to AlertEntity

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Optional: Add fields for KYC verification, etc.
}