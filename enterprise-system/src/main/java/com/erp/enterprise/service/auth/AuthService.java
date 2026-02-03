package com.erp.enterprise.service.auth;

import com.erp.enterprise.dto.auth.*;

/**
 * Authentication Service Interface
 */
public interface AuthService {

    JwtResponse login(LoginRequest loginRequest);

    MessageResponse register(RegisterRequest registerRequest);

    ProfileResponse getProfile(Long userId);

    ProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

    MessageResponse changePassword(Long userId, ChangePasswordRequest request);

    ProfileResponse updateSessionTimeout(Long userId, SessionTimeoutRequest request);
}