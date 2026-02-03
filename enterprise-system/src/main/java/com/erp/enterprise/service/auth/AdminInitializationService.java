package com.erp.enterprise.service.auth;

import com.erp.enterprise.entity.hr.Role;
import com.erp.enterprise.entity.hr.User;
import com.erp.enterprise.repository.auth.RoleRepository;
import com.erp.enterprise.repository.hr.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * User Initialization Service
 *
 * Security:
 * - All passwords read from environment variables
 * - No hardcoded credentials
 * - Creates one demo user per role for testing
 *
 * Required Environment Variables:
 * - ERP_ADMIN_PASSWORD (required)
 * - ERP_HR_PASSWORD (optional, defaults to ERP_ADMIN_PASSWORD)
 * - ERP_FINANCE_PASSWORD (optional, defaults to ERP_ADMIN_PASSWORD)
 * - ERP_SALES_PASSWORD (optional, defaults to ERP_ADMIN_PASSWORD)
 * - ERP_INVENTORY_PASSWORD (optional, defaults to ERP_ADMIN_PASSWORD)
 */
@Component
@Order(2)  // Run after RoleInitializationService
public class AdminInitializationService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializationService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Admin password - MUST be set in environment
    @Value("${erp.admin.password:#{null}}")
    private String adminPassword;

    // Module-specific passwords - fallback to admin password
    @Value("${erp.hr.password:#{null}}")
    private String hrPassword;

    @Value("${erp.finance.password:#{null}}")
    private String financePassword;

    @Value("${erp.sales.password:#{null}}")
    private String salesPassword;

    @Value("${erp.inventory.password:#{null}}")
    private String inventoryPassword;

    // Development mode flag - allows default passwords for local dev only
    @Value("${erp.dev.mode:false}")
    private boolean devMode;

    // Default dev password (only used when devMode=true)
    private static final String DEV_DEFAULT_PASSWORD = "Demo@123";

    @Override
    public void run(String... args) throws Exception {
        // Determine effective passwords
        String effectiveAdminPassword = resolvePassword(adminPassword, "admin");
        String effectiveHrPassword = resolvePassword(hrPassword, "hr", effectiveAdminPassword);
        String effectiveFinancePassword = resolvePassword(financePassword, "finance", effectiveAdminPassword);
        String effectiveSalesPassword = resolvePassword(salesPassword, "sales", effectiveAdminPassword);
        String effectiveInventoryPassword = resolvePassword(inventoryPassword, "inventory", effectiveAdminPassword);

        // Create demo users for each role
        createUserIfNotExists("admin", "admin@erp.com", effectiveAdminPassword, "ROLE_ADMIN");
        createUserIfNotExists("hr_user", "hr@erp.com", effectiveHrPassword, "ROLE_HR");
        createUserIfNotExists("finance_user", "finance@erp.com", effectiveFinancePassword, "ROLE_ACCOUNTANT");
        createUserIfNotExists("sales_user", "sales@erp.com", effectiveSalesPassword, "ROLE_SALES_STAFF");
        createUserIfNotExists("inventory_user", "inventory@erp.com", effectiveInventoryPassword, "ROLE_WAREHOUSE_STAFF");

        logger.info("User initialization complete");
        
        if (devMode) {
            logger.warn("========================================");
            logger.warn("DEVELOPMENT MODE ENABLED");
            logger.warn("Demo users created with default passwords");
            logger.warn("DO NOT USE IN PRODUCTION!");
            logger.warn("========================================");
        }
    }

    /**
     * Resolve password from environment or use dev default
     */
    private String resolvePassword(String password, String userType) {
        if (password != null && !password.isEmpty()) {
            return password;
        }
        
        if (devMode) {
            logger.warn("Using default dev password for {} user", userType);
            return DEV_DEFAULT_PASSWORD;
        }
        
        throw new IllegalStateException(
            "Password not configured for " + userType + " user. " +
            "Set erp." + userType + ".password or enable erp.dev.mode=true for development."
        );
    }

    /**
     * Resolve password with fallback to another password
     */
    private String resolvePassword(String password, String userType, String fallbackPassword) {
        if (password != null && !password.isEmpty()) {
            return password;
        }
        return fallbackPassword;
    }

    /**
     * Create user if doesn't exist, or update password if in dev mode
     */
   /**
 * Create user if doesn't exist, or update password if in dev mode
 */
    private void createUserIfNotExists(String username, String email, String password, String roleName) {
        // Check both username AND email to avoid constraint violations
        if (!userRepository.existsByUsername(username) && !userRepository.existsByEmail(email)) {
            // Create new user
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setIsActive(true);

            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role " + roleName + " not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);

            userRepository.save(user);
            logger.info("Created user: {} with role: {}", username, roleName);
        } else if (devMode) {
            // In dev mode, update existing user's password to match dev password
            User existingUser = userRepository.findByUsername(username).orElse(null);
            if (existingUser != null) {
                existingUser.setPassword(passwordEncoder.encode(password));
                userRepository.save(existingUser);
                logger.info("Updated password for existing user: {} (dev mode)", username);
            }
        } else {
            logger.info("User already exists - username: {} or email: {}", username, email);
        }
    }
}