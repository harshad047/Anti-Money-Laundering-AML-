package com.tss.aml.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendLoginSuccessEmailHtml(String toEmail) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String loginTime = LocalDateTime.now().format(formatter);

            String htmlContent = """
                <html>
                    <head>
                        <style>
                            body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                            .container { max-width: 500px; margin: auto; background: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
                            h2 { color: #333333; }
                            p { color: #555555; line-height: 1.5; }
                            .details { background-color: #f9f9f9; border-left: 4px solid #007bff; margin: 20px 0; padding: 15px; }
                            .security-note { font-size: 14px; color: #777777; margin-top: 20px; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h2>Successful Login to Your Account</h2>
                            <p>Hello,</p>
                            <p>This is a security notification to confirm a successful login to your account.</p>
                            <div class="details">
                                <strong>Time:</strong> """ + loginTime + """
                                <br>
                                <strong>Approximate Location:</strong> Rajkot, Gujarat, India
                            </div>
                            <p class="security-note">If you did not initiate this login, please change your password immediately and contact our support team.</p>
                        </div>
                    </body>
                </html>
            """;

            helper.setTo(toEmail);
            helper.setSubject("Security Alert: Successful Login to Your Account");
            helper.setText(htmlContent, true); 

            mailSender.send(mimeMessage);
            log.info("HTML Login success email sent to {}", toEmail);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send HTML login success email to {}", toEmail, e);
        }
    }
    
    public void sendRegistrationSuccessEmail(String toEmail, String firstName) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // 1. Define the template with a placeholder (%s)
            String htmlTemplate = """
                <html>
                    <head>
                        <style>
                            body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                            .container { max-width: 500px; margin: auto; background: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
                            h2 { color: #28a745; }
                            p { color: #555555; line-height: 1.5; }
                            .status { background-color: #fff3cd; border-left: 4px solid #ffc107; margin: 20px 0; padding: 15px; font-weight: bold; }
                            .cta-button { display: inline-block; background-color: #007bff; color: #ffffff; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin-top: 20px; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h2>Welcome, %s!</h2>
                            <p>Thank you for registering. Your account has been created successfully.</p>
                            <div class="status">
                                Your current status: KYC document upload pending.
                            </div>
                            <p>Please log in to your account and proceed to the dashboard to upload the necessary documents to fully activate your account.</p>
                            <a href="http://127.0.0.1:5500/login.html" class="cta-button">Go to Login</a>
                        </div>
                    </body>
                </html>
            """;

            String htmlContent = String.format(htmlTemplate, firstName);

            helper.setTo(toEmail);
            helper.setSubject("Welcome! Your Registration was Successful");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Registration success email sent to {}", toEmail);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send registration success email to {}", toEmail, e);
        }
    }
}
