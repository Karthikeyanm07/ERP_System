package com.erp.enterprise.controller.finance;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.finance.TransactionCreateRequest;
import com.erp.enterprise.dto.finance.TransactionDTO;
import com.erp.enterprise.service.finance.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Transaction Controller
 *
 * Base URL: /api/transactions
 *
 * Available endpoints:
 * - GET    /api/transactions                  -> Get all transactions
 * - GET    /api/transactions/{id}             -> Get by ID
 * - GET    /api/transactions/code/{code}      -> Get by code
 * - GET    /api/transactions/date/{date}      -> Get by date
 * - GET    /api/transactions/date-range       -> Get by date range
 * - GET    /api/transactions/user/{userId}    -> Get by user
 * - GET    /api/transactions/recent           -> Get recent transactions
 * - POST   /api/transactions                  -> Create transaction
 * - DELETE /api/transactions/{id}             -> Delete transaction
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * GET /api/transactions
     * Get all transactions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getAllTransactions() {
        List<TransactionDTO> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(
                ApiResponse.success("Transactions retrieved successfully", transactions)
        );
    }

    /**
     * GET /api/transactions/{id}
     * Get transaction by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransactionById(@PathVariable Long id) {
        TransactionDTO transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Transaction retrieved successfully", transaction)
        );
    }

    /**
     * GET /api/transactions/code/{code}
     * Get transaction by code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransactionByCode(@PathVariable String code) {
        TransactionDTO transaction = transactionService.getTransactionByCode(code);
        return ResponseEntity.ok(
                ApiResponse.success("Transaction retrieved successfully", transaction)
        );
    }

    /**
     * GET /api/transactions/date/{date}
     * Get transactions by date
     *
     * Example: /api/transactions/date/2026-01-15
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<TransactionDTO> transactions = transactionService.getTransactionsByDate(date);
        return ResponseEntity.ok(
                ApiResponse.success("Transactions by date retrieved successfully", transactions)
        );
    }

    /**
     * GET /api/transactions/date-range
     * Get transactions in date range
     *
     * Query params: startDate, endDate
     * Example: /api/transactions/date-range?startDate=2026-01-01&endDate=2026-01-31
     */
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<TransactionDTO> transactions = transactionService.getTransactionsByDateRange(
                startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success("Transactions in date range retrieved successfully", transactions)
        );
    }

    /**
     * GET /api/transactions/user/{userId}
     * Get transactions created by user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByUser(
            @PathVariable Long userId) {

        List<TransactionDTO> transactions = transactionService.getTransactionsByUser(userId);
        return ResponseEntity.ok(
                ApiResponse.success("User transactions retrieved successfully", transactions)
        );
    }

    /**
     * GET /api/transactions/recent
     * Get recent transactions (last 20)
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getRecentTransactions() {
        List<TransactionDTO> transactions = transactionService.getRecentTransactions();
        return ResponseEntity.ok(
                ApiResponse.success("Recent transactions retrieved successfully", transactions)
        );
    }

    /**
     * POST /api/transactions
     * Create new transaction
     *
     * Business Logic: Validates double-entry (Debits = Credits)
     */
    @PostMapping
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TransactionDTO>> createTransaction(
            @Valid @RequestBody TransactionCreateRequest request) {

        TransactionDTO transaction = transactionService.createTransaction(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created successfully", transaction));
    }

    /**
     * DELETE /api/transactions/{id}
     * Delete transaction
     *
     * Business Logic: Updates account balances after deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(
                ApiResponse.success("Transaction deleted successfully", null)
        );
    }
}