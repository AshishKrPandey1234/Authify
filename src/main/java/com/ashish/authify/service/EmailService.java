package com.ashish.authify.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Authify - Verify Your Account");

            // Clean HTML Email Template for a professional look
            String htmlContent = """
                <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #eee; max-width: 600px;">
                    <h2 style="color: #4CAF50;">Welcome to Authify!</h2>
                    <p>Thank you for registering. Please use the verification code below to activate your account:</p>
                    <div style="background: #f9f9f9; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #333; margin: 20px 0;">
                        %s
                    </div>
                    <p style="color: #777; font-size: 12px;">This OTP will expire in 5 minutes.</p>
                </div>
                """.formatted(otp);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}