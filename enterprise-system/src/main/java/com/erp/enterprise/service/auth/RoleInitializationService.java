package com.erp.enterprise.service.auth;

import com.erp.enterprise.entity.hr.Role;
import com.erp.enterprise.repository.auth.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import org.springframework.core.annotation.Order;

/**
 * Role Initialization Service
 *
 * Explanation:
 * - Runs automatically when application starts
 * - Creates default roles if they don't exist
 * - Ensures roles are available for user registration
 *
 * Default Roles:
 * - ROLE_USER: Basic user access
 * - ROLE_MANAGER: Manager-level access
 * - ROLE_HR: HR department access
 * - ROLE_ACCOUNTANT: Finance department access
 * - ROLE_WAREHOUSE_STAFF: Inventory management access
 * - ROLE_SALES_STAFF: Sales module access
 * - ROLE_ADMIN: Full system access
 */
@Component
@Order(1)  // Run FIRST, before AdminInitializationService (Order 2)
public class RoleInitializationService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RoleInitializationService.class);

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        createRoleIfNotExists("ROLE_USER", "Basic user role");
        createRoleIfNotExists("ROLE_MANAGER", "Manager role with elevated permissions");
        createRoleIfNotExists("ROLE_HR", "HR department role");
        createRoleIfNotExists("ROLE_ACCOUNTANT", "Finance/Accounting role");
        createRoleIfNotExists("ROLE_WAREHOUSE_STAFF", "Warehouse/Inventory management role");
        createRoleIfNotExists("ROLE_SALES_STAFF", "Sales department role");
        createRoleIfNotExists("ROLE_ADMIN", "Administrator role with full access");

        logger.info("Roles initialized successfully");
    }

    private void createRoleIfNotExists(String roleName, String description) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role();
            role.setName(roleName);
            role.setDescription(description);
            roleRepository.save(role);
            logger.info("Created role: {}", roleName);
        }
    }
}