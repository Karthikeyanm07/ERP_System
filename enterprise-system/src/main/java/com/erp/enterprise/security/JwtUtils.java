package com.erp.enterprise.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

/**
 * JWT Utility Class
 *
 * Explanation:
 * - Generates JWT tokens
 * - Validates JWT tokens
 * - Extracts username from token
 * - Uses HS512 algorithm for signing
 * 
 * Security:
 * - JWT secret MUST be set via environment variable
 * - Or enable dev mode for local development
 */
@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // JWT secret - MUST be set in environment (no default for security)
    @Value("${erp.app.jwtSecret:#{null}}")
    private String jwtSecret;

    // Token expiration time in milliseconds (24 hours)
    @Value("${erp.app.jwtExpirationMs:86400000}")
    private int jwtExpirationMs;

    // Dev mode flag
    @Value("${erp.dev.mode:false}")
    private boolean devMode;

    // Default dev secret (only used in dev mode)
    private static final String DEV_SECRET = "DevModeJWTSecretKeyForLocalDevelopmentOnlyDoNotUseInProduction2024";

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            if (devMode) {
                jwtSecret = DEV_SECRET;
                logger.warn("Using development JWT secret - DO NOT USE IN PRODUCTION!");
            } else {
                throw new IllegalStateException(
                    "JWT secret not configured. Set erp.app.jwtSecret or enable erp.dev.mode=true"
                );
            }
        }
        
        // Ensure secret is long enough for HS512
        if (jwtSecret.length() < 64) {
            logger.warn("JWT secret should be at least 64 characters for HS512");
        }
    }

    /**
     * Generate JWT token from authenticated user (uses default expiration).
     */
    public String generateJwtToken(Authentication authentication) {
        return generateJwtToken(authentication, jwtExpirationMs);
    }

    /**
     * Generate JWT token with custom expiration (e.g. from user's session timeout preference).
     *
     * @param expirationMs Expiration time in milliseconds from now
     */
    public String generateJwtToken(Authentication authentication, long expirationMs) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /** Default expiration in ms (for use when building response/cookie). */
    public int getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    /**
     * Get signing key from secret
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Extract username from JWT token
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser() // Changed from parserBuilder()
                .verifyWith((javax.crypto.SecretKey) getSigningKey()) // Changed from setSigningKey
                .build()
                .parseSignedClaims(token) // Changed from parseClaimsJws
                .getPayload() // Changed from getBody
                .getSubject();
    }


    /**
     * Validate JWT token
     *
     * Business Logic:
     * - Checks if token is properly signed
     * - Checks if token is expired
     * - Logs any validation errors
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}