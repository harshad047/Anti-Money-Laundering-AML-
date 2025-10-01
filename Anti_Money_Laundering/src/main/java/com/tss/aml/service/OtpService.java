package com.tss.aml.service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private final ConcurrentMap<String, OtpEntry> store = new ConcurrentHashMap<>();
    private final int EXPIRY_SECONDS = 300; // 5 minutes

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpToEmail(String email) {
        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        OtpEntry entry = new OtpEntry(otp, Instant.now().plusSeconds(EXPIRY_SECONDS));
        store.put(email, entry);

        // send email
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Your verification OTP");
        msg.setText("Your OTP for registration is: " + otp + " (valid for 5 minutes)");
        mailSender.send(msg);
    }

    public boolean verifyOtp(String email, String otp) {
        OtpEntry e = store.get(email);
        if (e == null) return false;
        if (Instant.now().isAfter(e.expiresAt)) {
            store.remove(email);
            return false;
        }
        boolean ok = e.otp.equals(otp);
        if (ok) store.remove(email);
        return ok;
    }

    private static class OtpEntry {
        final String otp;
        final Instant expiresAt;
        OtpEntry(String otp, Instant expiresAt) { this.otp = otp; this.expiresAt = expiresAt; }
    }
}
