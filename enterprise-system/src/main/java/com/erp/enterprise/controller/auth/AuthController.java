package com.erp.enterprise.controller.auth;

import com.erp.enterprise.dto.auth.*;
import com.erp.enterprise.security.UserDetailsImpl;
import com.erp.enterprise.service.auth.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 *
 * Base URL: /api/auth
 *
 * Available endpoints:
 * - POST /api/auth/login     -> User login (returns JWT in HttpOnly cookie)
 * - POST /api/auth/register  -> User registration (ROLE_USER only)
 * - POST /api/auth/logout    -> Clear JWT cookie
 *
 * Explanation:
 * - These endpoints are PUBLIC (no authentication required)
 * - Login sets JWT token in HttpOnly cookie (XSS-safe)
 * - User info returned in response body (without token for security)
 * - Registration only assigns ROLE_USER (elevated roles require admin)
 * 
 * Security:
 * - CORS handled by global SecurityConfig
 * - JWT stored in HttpOnly cookie (cannot be accessed by JavaScript)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Value("${erp.app.jwtExpirationMs:86400000}")
    private int jwtExpirationMs;

    @Value("${erp.app.cookie.secure:false}")
    private boolean cookieSecure;

    /**
     * POST /api/auth/login
     *
     * User Login - Sets JWT in HttpOnly cookie
     *
     * Request Body:
     * {
     *   "username": "admin",
     *   "password": "your_password"
     * }
     *
     * Response:
     * {
     *   "token": null, // Token is in cookie, not response body
     *   "type": "Bearer",
     *   "id": 1,
     *   "username": "admin",
     *   "email": "admin@erp.com",
     *   "roles": ["ROLE_ADMIN"]
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        
        JwtResponse jwtResponse = authService.login(loginRequest);
        
        // Set JWT token in HttpOnly cookie (use user's session timeout if returned)
        int maxAgeSeconds = jwtResponse.getExpiresInSeconds() != null
                ? jwtResponse.getExpiresInSeconds().intValue()
                : jwtExpirationMs / 1000;

        // Use ResponseCookie for modern browser security (SameSite support)
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("jwt", jwtResponse.getToken())
                .httpOnly(true)
                .secure(true) // Always secure in production cross-site
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("None") // REQUIRED for cross-site cookies (Vercel -> Render)
                .build();

        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());
        
        // Return response WITHOUT token (token is in cookie)
        JwtResponse safeResponse = new JwtResponse(
                null,  // Don't expose token in response body
                jwtResponse.getId(),
                jwtResponse.getUsername(),
                jwtResponse.getEmail(),
                jwtResponse.getRoles()
        );
        
        return ResponseEntity.ok(safeResponse);
    }

    /**
     * POST /api/auth/register
     *
     * User Registration - Only assigns ROLE_USER
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest) {

        MessageResponse messageResponse = authService.register(registerRequest);
        return ResponseEntity.ok(messageResponse);
    }

    /**
     * POST /api/auth/logout
     *
     * Logout - Clears JWT cookie
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletResponse response) {
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("jwt", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    /**
     * GET /api/auth/me
     * Returns current user profile (requires authentication).
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMe() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(authService.getProfile(userId));
    }

    /**
     * PATCH /api/auth/profile
     * Update current user's username and email (requires authentication).
     */
    @PatchMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(authService.updateProfile(userId, request));
    }

    /**
     * POST /api/auth/change-password
     * Change current user's password (requires authentication).
     */
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(authService.changePassword(userId, request));
    }

    /**
     * PATCH /api/auth/settings/session-timeout
     * Update current user's session timeout preference (requires authentication).
     */
    @PatchMapping("/settings/session-timeout")
    public ResponseEntity<ProfileResponse> updateSessionTimeout(@Valid @RequestBody SessionTimeoutRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(authService.updateSessionTimeout(userId, request));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            throw new com.erp.enterprise.exception.BusinessException("Not authenticated", "UNAUTHORIZED");
        }
        return ((UserDetailsImpl) auth.getPrincipal()).getId();
    }
}