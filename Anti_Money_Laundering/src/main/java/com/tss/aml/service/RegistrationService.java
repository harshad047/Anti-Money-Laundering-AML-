package com.tss.aml.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tss.aml.dto.RegistrationRequest;
import com.tss.aml.entity.Address;
import com.tss.aml.entity.Customer;
import com.tss.aml.entity.Document;
import com.tss.aml.entity.Enums.DocumentStatus;
import com.tss.aml.entity.Enums.KycStatus;
import com.tss.aml.entity.Enums.Role;
import com.tss.aml.repository.CustomerRepository;
import com.tss.aml.repository.DocumentRepository;

@Service
public class RegistrationService {

    @Autowired private CustomerRepository customerRepo;
    @Autowired private DocumentRepository docRepo;
    @Autowired private OtpService otpService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ReCaptchaService reCaptchaService;

    public void initiateEmailOtp(String email) {
        // optionally check uniqueness before sending OTP
        otpService.sendOtpToEmail(email);
    }

    public boolean verifyOtp(String email, String otp) {
        return otpService.verifyOtp(email, otp);
    }

    public Customer registerCustomer(RegistrationRequest req) {
        // Verify reCaptcha
        if (!reCaptchaService.verifyRecaptcha(req.getRecaptchaToken())) {
            throw new IllegalArgumentException("reCaptcha verification failed");
        }

        // check uniqueness
        if (customerRepo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        // Additional uniqueness checks can be added here if needed

        Customer c = new Customer();
        c.setFirstName(req.getFirstName());
        c.setMiddleName(req.getMiddleName());
        c.setLastName(req.getLastName());
        c.setEmail(req.getEmail());
        c.setPhone(req.getPhone());
        c.setPassword(passwordEncoder.encode(req.getPassword()));
        
        // Set role based on request or default to CUSTOMER
        Role userRole = Role.CUSTOMER;
        if (req.getRole() != null && req.getRole().equals("ADMIN")) {
            userRole = Role.ADMIN;
        }
        c.setRole(userRole);
        
        Address a = new Address();
        a.setStreet(req.getStreet()); a.setCity(req.getCity());
        a.setState(req.getState()); a.setCountry(req.getCountry());
        a.setPostalCode(req.getPostalCode());
        c.setAddress(a);
        c.setKycStatus(KycStatus.PENDING);
        return customerRepo.save(c);
    }

    public Document saveDocument(Long customerId, String docType, String storagePath) {
        Customer c = customerRepo.findById(customerId).orElseThrow();
        Document d = new Document();
        d.setDocType(docType);
        d.setStoragePath(storagePath);
        d.setCustomer(c);
        d.setStatus(DocumentStatus.UPLOADED);
        d.setUploadedAt(Instant.now());
        c.addDocument(d);
        customerRepo.save(c);
        return d;
    }
}
