package com.tss.aml.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tss.aml.entity.Customer;
import com.tss.aml.repository.CustomerRepository;

@RestController
@RequestMapping("/api/customer")
@PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/profile")
    public ResponseEntity<Customer> getProfile(Authentication authentication) {
        String email = authentication.getName();
        Customer customer = customerRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/kyc-status")
    public ResponseEntity<?> getKycStatus(Authentication authentication) {
        String email = authentication.getName();
        Customer customer = customerRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        KycStatusResponse response = new KycStatusResponse(
            customer.getKycStatus().name(),
            "Your KYC status is: " + customer.getKycStatus()
        );
        return ResponseEntity.ok(response);
    }
    
    public static class KycStatusResponse {
        public final String kycStatus;
        public final String message;
        
        public KycStatusResponse(String kycStatus, String message) {
            this.kycStatus = kycStatus;
            this.message = message;
        }
    }
}
