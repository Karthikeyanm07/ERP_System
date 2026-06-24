package com.erp.enterprise.service.auth;

import com.erp.enterprise.dto.auth.*;

/**
 * Authentication Service Interface
 */
public interface AuthService {

    JwtResponse login(LoginRequest loginRequest);

    MessageResponse register(RegisterRequest registerRequest);

    ProfileResponse getProfile(@org.springframework.lang.NonNull Long userId);
 
    ProfileResponse updateProfile(@org.springframework.lang.NonNull Long userId, UpdateProfileRequest request);
 
    MessageResponse changePassword(@org.springframework.lang.NonNull Long userId, ChangePasswordRequest request);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);

    MessageResponse validateResetToken(String token);
 
    ProfileResponse updateSessionTimeout(@org.springframework.lang.NonNull Long userId, SessionTimeoutRequest request);
}