package com.erp.enterprise.service.auth.impl;

import com.erp.enterprise.service.auth.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${erp.mail.from:${spring.mail.username:}}")
    private String fromOverride;

    @Override
    public void sendPasswordResetEmail(String toEmail, String username, String resetLink) {
        String from = !fromOverride.isBlank() ? fromOverride : fromAddress;
        String subject = "Reset your ERP password";
        String body = """
                Hello %s,

                We received a request to reset your ERP account password.

                Click the link below to choose a new password (valid for 1 hour):
                %s

                If you did not request this, you can ignore this email.

                ERP System
                """.formatted(username, resetLink);

        if (mailSender == null || from.isBlank()) {
            logger.warn(
                    "Mail is not configured. Password reset link for {} ({}): {}",
                    username,
                    toEmail,
                    resetLink
            );
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
            logger.info("Password reset email sent to {}", toEmail);
        } catch (Exception ex) {
            logger.error("Failed to send password reset email to {}", toEmail, ex);
        }
    }
}
