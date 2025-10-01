package com.tss.aml.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tss.aml.dto.AuthResponse;
import com.tss.aml.dto.LoginInitRequest;
import com.tss.aml.dto.LoginVerifyRequest; // Using the specific DTO for OTP verification
import com.tss.aml.entity.Customer;
import com.tss.aml.repository.CustomerRepository;
import com.tss.aml.service.OtpService;
import com.tss.aml.util.JwtUtil;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private OtpService otpService;

    /**
     * Step 1: Validates credentials and sends an OTP to the user's email.
     */
    @PostMapping("/login/init")
    public ResponseEntity<?> loginInit(@Valid @RequestBody LoginInitRequest loginInitRequest) {
        try {
            // Verify credentials (email and password)
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginInitRequest.getEmail(),
                    loginInitRequest.getPassword()
                )
            );

            // Send OTP to email
            otpService.sendOtpToEmail(loginInitRequest.getEmail());
            
            return ResponseEntity.ok("OTP sent to your email. Please verify to complete login.");
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("Invalid credentials.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("An error occurred during login initiation.");
        }
    }

    /**
     * Step 2: Verifies the OTP and completes the login by issuing a JWT.
     */
    @PostMapping("/login/verify")
    public ResponseEntity<?> loginVerify(@Valid @RequestBody LoginVerifyRequest loginRequest) {
        try {
            // Verify OTP
            if (!otpService.verifyOtp(loginRequest.getEmail(), loginRequest.getOtp())) {
                return ResponseEntity.badRequest().body("Invalid or expired OTP.");
            }

            // Get customer details
            Customer customer = customerRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + loginRequest.getEmail()));

            // Generate JWT token
            String token = jwtUtil.generateToken(customer.getEmail(), customer.getRole().name());
            
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
}