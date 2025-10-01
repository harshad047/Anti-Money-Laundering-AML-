package com.tss.aml.controller;

import com.tss.aml.entity.Transaction;
import com.tss.aml.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService txService;

    @PostMapping("/initiate")
    public Transaction initiate(
            @RequestParam String customerId,
            @RequestParam BigDecimal amount,
            @RequestParam String currency,
            @RequestParam(required = false) String receiverCountryCode,
            @RequestParam String description) {
        return txService.initiate(customerId, amount, currency, receiverCountryCode, description);
    }
}