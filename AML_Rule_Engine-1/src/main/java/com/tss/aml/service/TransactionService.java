package com.tss.aml.service;

import com.tss.aml.entity.Transaction;
import com.tss.aml.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository txRepo;
    private final NLPService nlpService;
    private final RuleEngineService ruleEngine;

    @Transactional
    public Transaction initiate(String customerId, BigDecimal amount, String currency,
                                String country, String desc) {
        // Step 1: NLP
        int nlp = getNlpScore(desc);

        // Step 2: Rules
        var input = RuleEngineService.TransactionInput.builder()
                .txId("TEMP")
                .amount(amount)
                .countryCode(country)
                .nlpScore(nlp)
                .text(desc)
                .build();
        var ruleResult = ruleEngine.evaluate(input);

        // Step 3: Combined Score (0–100)
        int combined = Math.min((nlp + ruleResult.getTotalRiskScore()) / 2, 100);

        // Step 4: Multi-tier threshold logic
        String status;
        boolean exceeds = false;
        String alertId = null;

        if (combined >= 90) {
            status = "BLOCKED";
            exceeds = true;
            alertId = "ALERT_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } else if (combined >= 60) {
            status = "FLAGGED";
            exceeds = true;
            alertId = "ALERT_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } else {
            status = "APPROVED";
            exceeds = false;
            alertId = null;
        }

        // Step 5: Build Transaction
        Transaction tx = Transaction.builder()
                .customerId(customerId)
                .amount(amount)
                .currency(currency)
                .receiverCountryCode(country)
                .description(desc)
                .nlpScore(nlp)
                .ruleEngineScore(ruleResult.getTotalRiskScore())
                .combinedRiskScore(combined)
                .thresholdExceeded(exceeds)
                .status(status)
                .alertId(alertId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return txRepo.save(tx);
    }

    private int getNlpScore(String text) {
        try {
            return (int) nlpService.analyzeText(text).get("nlpScore");
        } catch (Exception e) {
            return 0;
        }
    }
}