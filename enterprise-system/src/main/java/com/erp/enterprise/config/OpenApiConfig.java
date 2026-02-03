package com.erp.enterprise.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger Configuration
 *
 * Explanation:
 * - Configures Swagger UI for API documentation
 * - Defines API metadata (title, version, description)
 * - Configures JWT authentication in Swagger UI
 * - Provides server information
 *
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Enterprise ERP System API",
                version = "1.0.0",
                description = """
            # Enterprise Resource Planning (ERP) System API Documentation
            
            A comprehensive ERP system built with Spring Boot that integrates HR, Finance, Inventory, and Sales modules.
            
            ## Features
            - **HR Module**: Employee management, attendance tracking, leave management
            - **Finance Module**: Chart of accounts, double-entry bookkeeping, expense tracking
            - **Inventory Module**: Product catalog, stock management, purchase orders
            - **Sales Module**: Customer management, sales orders, invoicing, payments
            - **Authentication**: JWT-based authentication with role-based access control
            
            ## Authentication
            Most endpoints require authentication. Follow these steps:
            1. Register a new user or use default admin credentials (username: `admin`, password: `admin123`)
            2. Call the `/api/auth/login` endpoint to get a JWT token
            3. Click the "Authorize" button (🔒) at the top of this page
            4. Enter your token in the format: `Bearer <your-token-here>`
            5. Click "Authorize" and then "Close"
            6. Now you can test all authenticated endpoints!
            
            ## Available Roles
            - **ROLE_ADMIN**: Full system access
            - **ROLE_HR**: HR module access
            - **ROLE_ACCOUNTANT**: Finance module access
            - **ROLE_WAREHOUSE_STAFF**: Inventory module access
            - **ROLE_SALES_STAFF**: Sales module access
            - **ROLE_MANAGER**: Manager-level access
            - **ROLE_USER**: Basic user access
            
            ## Getting Started
            1. Start with authentication endpoints (`/api/auth`)
            2. Login to get your JWT token
            3. Use the token to access other endpoints
            4. Explore each module's endpoints below
            
            ## Support
            For issues or questions, please contact the development team.
            """,
                contact = @Contact(
                        name = "ERP Development Team",
                        email = "support@erp.com",
                        url = "https://erp.com/support"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Local Development Server"
                ),
                @Server(
                        url = "https://api.erp.com",
                        description = "Production Server"
                )
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        description = """
        JWT Authentication Token
        
        To get a token:
        1. Use the /api/auth/login endpoint
        2. Copy the token from the response
        3. Click 'Authorize' button and paste: Bearer <token>
        
        Example: Bearer eyJhbGciOiJIUzUxMiJ9...
        """,
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    /**
     * Group: Authentication APIs
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("1-authentication")
                .displayName("1. Authentication & Security")
                .pathsToMatch("/api/auth/**", "/api/test/**")
                .build();
    }

    /**
     * Group: HR Module APIs
     */
    @Bean
    public GroupedOpenApi hrApi() {
        return GroupedOpenApi.builder()
                .group("2-hr-module")
                .displayName("2. HR Module")
                .pathsToMatch(
                        "/api/departments/**",
                        "/api/employees/**",
                        "/api/attendance/**",
                        "/api/leave-types/**",
                        "/api/leave-requests/**"
                )
                .build();
    }

    /**
     * Group: Finance Module APIs
     */
    @Bean
    public GroupedOpenApi financeApi() {
        return GroupedOpenApi.builder()
                .group("3-finance-module")
                .displayName("3. Finance Module")
                .pathsToMatch(
                        "/api/accounts/**",
                        "/api/transactions/**",
                        "/api/expenses/**"
                )
                .build();
    }

    /**
     * Group: Inventory Module APIs
     */
    @Bean
    public GroupedOpenApi inventoryApi() {
        return GroupedOpenApi.builder()
                .group("4-inventory-module")
                .displayName("4. Inventory Module")
                .pathsToMatch(
                        "/api/categories/**",
                        "/api/suppliers/**",
                        "/api/warehouses/**",
                        "/api/products/**",
                        "/api/stock/**",
                        "/api/purchase-orders/**",
                        "/api/stock-movements/**"
                )
                .build();
    }

    /**
     * Group: Sales Module APIs
     */
    @Bean
    public GroupedOpenApi salesApi() {
        return GroupedOpenApi.builder()
                .group("5-sales-module")
                .displayName("5. Sales Module")
                .pathsToMatch(
                        "/api/customers/**",
                        "/api/sales-orders/**",
                        "/api/invoices/**",
                        "/api/payments/**"
                )
                .build();
    }

    /**
     * Group: All APIs
     */
    @Bean
    public GroupedOpenApi allApis() {
        return GroupedOpenApi.builder()
                .group("0-all-apis")
                .displayName("All APIs")
                .pathsToMatch("/api/**")
                .build();
    }
}