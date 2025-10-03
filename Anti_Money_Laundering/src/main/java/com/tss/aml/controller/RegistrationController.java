package com.tss.aml.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tss.aml.dto.RegistrationRequest;
import com.tss.aml.entity.Customer;
import com.tss.aml.entity.Document;
import com.tss.aml.service.CloudinaryService;
import com.tss.aml.service.EmailService;
import com.tss.aml.service.RegistrationService;
import com.tss.aml.util.JwtUtil;

import io.jsonwebtoken.io.IOException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/register")
@CrossOrigin(origins = "http://127.0.0.1:5500") // Allow requests from your Live Server origin
public class RegistrationController {

    @Autowired private RegistrationService regService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private EmailService emailService;

    // 1. send OTP to email (AJAX)
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {
        // validate email format
        regService.initiateEmailOtp(email);
        return ResponseEntity.ok(Map.of("message","OTP sent"));
    }

    // 2. verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        boolean ok = regService.verifyOtp(email, otp);
        if (ok) return ResponseEntity.ok(Map.of("verified", true));
        else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("verified", false));
    }

    // 3. register after OTP verified (body = RegistrationRequest DTO)
    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequest req, BindingResult br) {
        if (br.hasErrors()) {
            return ResponseEntity.badRequest().body(br.getAllErrors());
        }
        
        try {
            // This creates the customer in the database
            Customer created = regService.registerCustomer(req);
            
            // 3. SEND THE REGISTRATION SUCCESS EMAIL
            emailService.sendRegistrationSuccessEmail(created.getEmail(), created.getFirstName());

            // Generate a token so the user can be logged in immediately
            String token = jwtUtil.generateToken(created.getEmail(), created.getRole().name());
            
            return ResponseEntity.ok(Map.of(
                "customerId", created.getId(),
                "token", token,
                "role", created.getRole().name(),
                "message", "Registration successful"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping(path="/{customerId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocuments(
            @PathVariable Long customerId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("docType") String docType) throws IOException, java.io.IOException {

        String folder = "customer_" + customerId;
        String cloudUrl = cloudinaryService.uploadFile(file, folder);

        Document d = regService.saveDocument(customerId, docType, cloudUrl);
        return ResponseEntity.ok(Map.of("documentId", d.getId(), "url", cloudUrl));
    }

}
