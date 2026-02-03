package com.erp.enterprise.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * JWT Response DTO

 * Explanation:
 * - Returned after successful login
 * - Contains JWT token and user information
 * - Frontend stores token and sends it with each request
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    private String token;
    private String type = "Bearer";  // Token type
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    /** Cookie/token expiration in seconds (for frontend or cookie maxAge). */
    private Long expiresInSeconds;

    public JwtResponse(String token, Long id, String username, String email, List<String> roles) {
        this(token, id, username, email, roles, null);
    }

    public JwtResponse(String token, Long id, String username, String email, List<String> roles, Long expiresInSeconds) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.expiresInSeconds = expiresInSeconds;
    }
}