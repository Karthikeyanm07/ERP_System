package com.erp.enterprise.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO for current user profile (GET /api/auth/me, PATCH /api/auth/profile).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private Integer sessionTimeoutMinutes;
}
