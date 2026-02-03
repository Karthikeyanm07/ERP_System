package com.erp.enterprise.controller.finance;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.finance.AccountDTO;
import com.erp.enterprise.service.finance.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Account Controller - Chart of Accounts Management
 *
 * Base URL: /api/accounts
 *
 * Available endpoints:
 * - GET    /api/accounts                    -> Get all accounts
 * - GET    /api/accounts/{id}               -> Get by ID
 * - GET    /api/accounts/code/{code}        -> Get by code
 * - GET    /api/accounts/type/{type}        -> Get by type
 * - GET    /api/accounts/active             -> Get active accounts
 * - GET    /api/accounts/top-level          -> Get top-level accounts
 * - GET    /api/accounts/{id}/children      -> Get child accounts
 * - GET    /api/accounts/search?keyword=xyz -> Search accounts
 * - POST   /api/accounts                    -> Create account
 * - PUT    /api/accounts/{id}               -> Update account
 * - DELETE /api/accounts/{id}               -> Delete account
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * GET /api/accounts
     * Get all accounts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getAllAccounts() {
        List<AccountDTO> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(
                ApiResponse.success("Accounts retrieved successfully", accounts)
        );
    }

    /**
     * GET /api/accounts/{id}
     * Get account by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDTO>> getAccountById(@PathVariable Long id) {
        AccountDTO account = accountService.getAccountById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Account retrieved successfully", account)
        );
    }

    /**
     * GET /api/accounts/code/{code}
     * Get account by code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<AccountDTO>> getAccountByCode(@PathVariable String code) {
        AccountDTO account = accountService.getAccountByCode(code);
        return ResponseEntity.ok(
                ApiResponse.success("Account retrieved successfully", account)
        );
    }

    /**
     * GET /api/accounts/type/{type}
     * Get accounts by type
     *
     * Valid types: ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getAccountsByType(@PathVariable String type) {
        List<AccountDTO> accounts = accountService.getAccountsByType(type);
        return ResponseEntity.ok(
                ApiResponse.success("Accounts by type retrieved successfully", accounts)
        );
    }

    /**
     * GET /api/accounts/active
     * Get all active accounts
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getActiveAccounts() {
        List<AccountDTO> accounts = accountService.getActiveAccounts();
        return ResponseEntity.ok(
                ApiResponse.success("Active accounts retrieved successfully", accounts)
        );
    }

    /**
     * GET /api/accounts/top-level
     * Get top-level accounts (no parent)
     */
    @GetMapping("/top-level")
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getTopLevelAccounts() {
        List<AccountDTO> accounts = accountService.getTopLevelAccounts();
        return ResponseEntity.ok(
                ApiResponse.success("Top-level accounts retrieved successfully", accounts)
        );
    }

    /**
     * GET /api/accounts/{id}/children
     * Get child accounts of a parent account
     */
    @GetMapping("/{id}/children")
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getChildAccounts(@PathVariable Long id) {
        List<AccountDTO> accounts = accountService.getChildAccounts(id);
        return ResponseEntity.ok(
                ApiResponse.success("Child accounts retrieved successfully", accounts)
        );
    }

    /**
     * GET /api/accounts/search?keyword=xyz
     * Search accounts by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<AccountDTO>>> searchAccounts(
            @RequestParam(required = false) String keyword) {

        List<AccountDTO> accounts = accountService.searchAccounts(keyword);
        return ResponseEntity.ok(
                ApiResponse.success("Search completed successfully", accounts)
        );
    }

    /**
     * POST /api/accounts
     * Create new account
     */
    @PostMapping
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AccountDTO>> createAccount(
            @Valid @RequestBody AccountDTO accountDTO) {

        AccountDTO createdAccount = accountService.createAccount(accountDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", createdAccount));
    }

    /**
     * PUT /api/accounts/{id}
     * Update account
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AccountDTO>> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountDTO accountDTO) {

        AccountDTO updatedAccount = accountService.updateAccount(id, accountDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Account updated successfully", updatedAccount)
        );
    }

    /**
     * DELETE /api/accounts/{id}
     * Delete account
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")  // Only ADMIN can delete accounts
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok(
                ApiResponse.success("Account deleted successfully", null)
        );
    }
}