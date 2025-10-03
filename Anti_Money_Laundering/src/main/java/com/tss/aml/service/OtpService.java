package com.tss.aml.service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class OtpService {

    private final ConcurrentMap<String, OtpEntry> store = new ConcurrentHashMap<>();
    private final int EXPIRY_SECONDS = 300; // 5 minutes

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpToEmail(String email) {
        try {
            String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
            OtpEntry entry = new OtpEntry(otp, Instant.now().plusSeconds(EXPIRY_SECONDS));
            store.put(email, entry);

            String htmlContent = """
                <html>
                    <head>
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                background-color: #f4f4f4;
                                padding: 20px;
                            }
                            .container {
                                max-width: 500px;
                                margin: auto;
                                background: #ffffff;
                                padding: 20px;
                                border-radius: 10px;
                                box-shadow: 0 0 10px rgba(0,0,0,0.1);
                            }
                            h2 {
                                color: #333333;
                            }
                            .otp {
                                font-size: 24px;
                                font-weight: bold;
                                color: #007bff;
                                margin: 20px 0;
                            }
                            .note {
                                font-size: 14px;
                                color: #555555;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h2>Your Verification OTP</h2>
                            <p>Use the OTP below to complete your registration:</p>
                            <div class="otp">""" + otp + """
                            <p class="note">This OTP is valid for 5 minutes.</p>
                        </div>
                    </body>
                </html>
            """;

            // Send as HTML email
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Your verification OTP");
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
