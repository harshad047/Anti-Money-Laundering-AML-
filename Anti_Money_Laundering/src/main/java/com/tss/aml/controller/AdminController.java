package com.tss.aml.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tss.aml.entity.Customer;
import com.tss.aml.entity.Enums.KycStatus;
import com.tss.aml.repository.CustomerRepository;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        return ResponseEntity.ok(customer);
    }

    @PutMapping("/customers/{id}/kyc-status")
    public ResponseEntity<?> updateKycStatus(@PathVariable Long id, @PathVariable KycStatus status) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setKycStatus(status);
        customerRepository.save(customer);
        return ResponseEntity.ok("KYC status updated successfully");
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        long totalCustomers = customerRepository.count();
        long pendingKyc = customerRepository.countByKycStatus(KycStatus.PENDING);
        long approvedKyc = customerRepository.countByKycStatus(KycStatus.APPROVED);
        long rejectedKyc = customerRepository.countByKycStatus(KycStatus.REJECTED);
        
        DashboardStats stats = new DashboardStats(totalCustomers, pendingKyc, approvedKyc, rejectedKyc);
        return ResponseEntity.ok(stats);
    }
    
    public static class DashboardStats {
        public final long totalCustomers;
        public final long pendingKyc;
        public final long approvedKyc;
        public final long rejectedKyc;
        
        public DashboardStats(long totalCustomers, long pendingKyc, long approvedKyc, long rejectedKyc) {
            this.totalCustomers = totalCustomers;
            this.pendingKyc = pendingKyc;
            this.approvedKyc = approvedKyc;
            this.rejectedKyc = rejectedKyc;
        }
    }
}
