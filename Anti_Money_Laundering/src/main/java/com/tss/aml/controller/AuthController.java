package com.tss.aml.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tss.aml.dto.AuthResponse;
import com.tss.aml.dto.LoginRequest;
import com.tss.aml.dto.LoginInitRequest;
import com.tss.aml.entity.Customer;
import com.tss.aml.repository.CustomerRepository;
import com.tss.aml.service.ReCaptchaService;
import com.tss.aml.service.OtpService;
import com.tss.aml.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ReCaptchaService reCaptchaService;
    
    @Autowired
    private OtpService otpService;

    // Step 1: Initiate login with credentials and reCaptcha
    @PostMapping("/login/init")
    public ResponseEntity<?> loginInit(@Valid @RequestBody LoginInitRequest loginInitRequest) {
        try {
            // Verify reCaptcha
            if (!reCaptchaService.verifyRecaptcha(loginInitRequest.getRecaptchaToken())) {
                return ResponseEntity.badRequest().body("reCaptcha verification failed");
            }

            // Verify credentials
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginInitRequest.getEmail(),
                    loginInitRequest.getPassword()
                )
            );

            // Send OTP to email
            otpService.sendOtpToEmail(loginInitRequest.getEmail());
            
            return ResponseEntity.ok("OTP sent to your email. Please verify to complete login.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid credentials or reCaptcha verification failed");
        }
    }

    // Step 2: Complete login with OTP verification
    @PostMapping("/login/verify")
    public ResponseEntity<?> loginVerify(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Verify OTP
            if (!otpService.verifyOtp(loginRequest.getEmail(), loginRequest.getOtp())) {
                return ResponseEntity.badRequest().body("Invalid OTP");
            }

            // Verify reCaptcha again for security
            if (!reCaptchaService.verifyRecaptcha(loginRequest.getRecaptchaToken())) {
                return ResponseEntity.badRequest().body("reCaptcha verification failed");
            }

            // Get customer details
            Customer customer = customerRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate JWT token
            String token = jwtUtil.generateToken(loginRequest.getEmail(), customer.getRole().name());
            
            return ResponseEntity.ok(new AuthResponse(
                token,
                "Bearer",
                customer.getEmail(),
                customer.getRole().name(),
                customer.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login verification failed: " + e.getMessage());
        }
    }

    // Legacy login endpoint (for backward compatibility)
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return loginVerify(loginRequest);
    }
}
