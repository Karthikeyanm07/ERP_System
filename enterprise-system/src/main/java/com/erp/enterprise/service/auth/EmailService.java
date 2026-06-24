package com.erp.enterprise.service.auth;

public interface EmailService {

    void sendPasswordResetEmail(String toEmail, String username, String resetLink);
}
