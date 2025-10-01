package com.tss.aml.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RuleCondition {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule rule;

    @Enumerated(EnumType.STRING)
    private ConditionType type;

    private String field;
    private String operator;
    private String value;
    private boolean isActive = true;

    public enum ConditionType {
        AMOUNT, COUNTRY_RISK, NLP_SCORE, KEYWORD_MATCH
    }
}