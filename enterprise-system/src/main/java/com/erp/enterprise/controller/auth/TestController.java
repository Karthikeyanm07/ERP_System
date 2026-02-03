package com.erp.enterprise.controller.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test Controller - For Testing Role-Based Access Control
 *
 * Base URL: /api/test
 *
 * Explanation:
 * - These endpoints demonstrate RBAC
 * - @PreAuthorize checks if user has required role
 * - Used to test authentication and authorization
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * Public endpoint - No authentication required
     */
    @GetMapping("/public")
    public String publicAccess() {
        return "Public content - accessible to everyone";
    }

    /**
     * User endpoint - Requires authentication (any role)
     */
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public String userAccess() {
        return "User content - accessible to authenticated users";
    }

    /**
     * Manager endpoint - Requires MANAGER or ADMIN role
     */
    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public String managerAccess() {
        return "Manager content - accessible to managers and admins";
    }

    /**
     * Admin endpoint - Requires ADMIN role only
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Admin content - accessible to admins only";
    }
}