package com.tss.aml.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.tss.aml.entity.CountryRisk;
import com.tss.aml.entity.Rule;
import com.tss.aml.entity.RuleCondition;
import com.tss.aml.entity.RuleExecutionLog;
import com.tss.aml.repository.CountryRiskRepository;
import com.tss.aml.repository.RuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RuleEngineService {

    private final RuleRepository ruleRepo;
    private final CountryRiskRepository countryRepo;

    public EvaluationResult evaluate(TransactionInput input) {
        List<Rule> rules = ruleRepo.findByIsActiveTrueOrderByPriorityAsc();
        List<RuleExecutionLog> logs = new ArrayList<>();
        double survivalProb = 1.0; // P(no risk) = ∏(1 - P_i)

        String cleanText = input.getText() != null ? cleanForMatching(input.getText()) : "";

        for (Rule rule : rules) {
            boolean match = true;
            for (RuleCondition cond : rule.getConditions()) {
                if (!cond.isActive()) continue;
                if (!evaluateCondition(cond, input, cleanText)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                // Convert rule weight (0–100) → probability (0.0–1.0)
                double ruleProb = Math.min(1.0, Math.max(0.0, rule.getRiskWeight() / 100.0));
                survivalProb *= (1.0 - ruleProb); // Multiply survival probabilities

                logs.add(RuleExecutionLog.builder()
                        .rule(rule)
                        .transactionId(input.getTxId())
                        .matched(true)
                        .details("Rule triggered: " + rule.getName())
                        .evaluatedAt(java.time.LocalDateTime.now())
                        .build());
            }
        }

        // Final risk probability
        double combinedProb = 1.0 - survivalProb;
        int ruleEngineScore = (int) Math.round(combinedProb * 100);

        return new EvaluationResult(ruleEngineScore, logs);
    }

    private boolean evaluateCondition(RuleCondition cond, TransactionInput input, String cleanText) {
        switch (cond.getType()) {
            case AMOUNT:
                return compareNumber(input.getAmount(), cond.getOperator(), cond.getValue());
            case COUNTRY_RISK:
                CountryRisk cr = countryRepo.findByCountryCode(input.getCountryCode());
                return cr != null && compareNumber(cr.getRiskScore(), cond.getOperator(), cond.getValue());
            case NLP_SCORE:
                return compareNumber(input.getNlpScore(), cond.getOperator(), cond.getValue());
            case KEYWORD_MATCH:
                if (cleanText.isEmpty()) return false;
                String keyword = cond.getValue().toLowerCase();
                return containsWholeWord(cleanText, keyword);
            default:
                return false;
        }
    }

    private String cleanForMatching(String text) {
        return text.toLowerCase()
                   .replaceAll("[^a-z0-9\\s]", " ")
                   .trim()
                   .replaceAll("\\s+", " ");
    }

    private boolean containsWholeWord(String text, String word) {
        String pattern = "\\b" + Pattern.quote(word) + "\\b";
        return Pattern.compile(pattern).matcher(text).find();
    }

    private boolean compareNumber(Number actual, String operator, String expectedStr) {
        if (actual == null) return false;
        try {
            double actualVal = actual.doubleValue();
            double expected = Double.parseDouble(expectedStr);
            return switch (operator) {
                case ">" -> actualVal > expected;
                case ">=" -> actualVal >= expected;
                case "<" -> actualVal < expected;
                case "<=" -> actualVal <= expected;
                case "==" -> Math.abs(actualVal - expected) < 1e-6;
                default -> false;
            };
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class TransactionInput {
        private String txId;
        private BigDecimal amount;
        private String countryCode;
        private Integer nlpScore;
        private String text;
    }

    @lombok.Value
    public static class EvaluationResult {
        int totalRiskScore; // Now: probabilistic fusion of all triggered rules
        List<RuleExecutionLog> triggeredRules;
    }
}