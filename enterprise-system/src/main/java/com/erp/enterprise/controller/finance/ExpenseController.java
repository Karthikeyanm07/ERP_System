package com.erp.enterprise.controller.finance;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.finance.ExpenseCreateRequest;
import com.erp.enterprise.dto.finance.ExpenseDTO;
import com.erp.enterprise.service.finance.ExpenseService;
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
 * Expense Controller
 * <p>
 * Base URL: /api/expenses
 * <p>
 * Available endpoints:
 * - GET    /api/expenses                        -> Get all expenses
 * - GET    /api/expenses/{id}                   -> Get by ID
 * - GET    /api/expenses/code/{code}            -> Get by code
 * - GET    /api/expenses/employee/{employeeId}  -> Get by employee
 * - GET    /api/expenses/status/{status}        -> Get by status
 * - GET    /api/expenses/category/{category}    -> Get by category
 * - GET    /api/expenses/date-range             -> Get by date range
 * - POST   /api/expenses                        -> Create expense
 * - PUT    /api/expenses/{id}                   -> Update expense
 * - PUT    /api/expenses/{id}/approve           -> Approve expense
 * - PUT    /api/expenses/{id}/reject            -> Reject expense
 * - PUT    /api/expenses/{id}/paid              -> Mark as paid
 * - DELETE /api/expenses/{id}                   -> Delete expense
 */
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * GET /api/expenses
     * Get all expenses
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseDTO>>> getAllExpenses() {
        List<ExpenseDTO> expenses = expenseService.getAllExpenses();
        return ResponseEntity.ok(
                ApiResponse.success("Expenses retrieved successfully", expenses)
        );
    }

    /**
     * GET /api/expenses/{id}
     * Get expense by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseDTO>> getExpenseById(@PathVariable Long id) {
        ExpenseDTO expense = expenseService.getExpenseById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Expense retrieved successfully", expense)
        );
    }

    /**
     * GET /api/expenses/code/{code}
     * Get expense by code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<ExpenseDTO>> getExpenseByCode(@PathVariable String code) {
        ExpenseDTO expense = expenseService.getExpenseByCode(code);
        return ResponseEntity.ok(
                ApiResponse.success("Expense retrieved successfully", expense)
        );
    }

    /**
     * GET /api/expenses/employee/{employeeId}
     * Get expenses by employee
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<ExpenseDTO>>> getExpensesByEmployee(
            @PathVariable Long employeeId) {

        List<ExpenseDTO> expenses = expenseService.getExpensesByEmployee(employeeId);
        return ResponseEntity.ok(
                ApiResponse.success("Employee expenses retrieved successfully", expenses)
        );
    }

    /**
     * GET /api/expenses/status/{status}
     * Get expenses by status
     * <p>
     * Valid statuses: PENDING, APPROVED, REJECTED, PAID
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ExpenseDTO>>> getExpensesByStatus(
            @PathVariable String status) {

        List<ExpenseDTO> expenses = expenseService.getExpensesByStatus(status);
        return ResponseEntity.ok(
                ApiResponse.success("Expenses by status retrieved successfully", expenses)
        );
    }

    /**
     * GET /api/expenses/category/{category}
     * Get expenses by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ExpenseDTO>>> getExpensesByCategory(
            @PathVariable String category) {

        List<ExpenseDTO> expenses = expenseService.getExpensesByCategory(category);
        return ResponseEntity.ok(
                ApiResponse.success("Expenses by category retrieved successfully", expenses)
        );
    }

    /**
     * GET /api/expenses/date-range
     * Get expenses in date range
     * <p>
     * Query params: startDate, endDate
     */
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<ExpenseDTO>>> getExpensesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ExpenseDTO> expenses = expenseService.getExpensesByDateRange(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success("Expenses in date range retrieved successfully", expenses)
        );
    }

    /**
     * POST /api/expenses
     * Create new expense
     */
    @PostMapping
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExpenseDTO>> createExpense(
            @Valid @RequestBody ExpenseCreateRequest request) {

        ExpenseDTO expense = expenseService.createExpense(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense created successfully", expense));
    }

    /**
     * PUT /api/expenses/{id}
     * Update expense (only PENDING expenses can be updated)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExpenseDTO>> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDTO expenseDTO) {

        ExpenseDTO updatedExpense = expenseService.updateExpense(id, expenseDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Expense updated successfully", updatedExpense)
        );
    }

    /**
     * PUT /api/expenses/{id}/approve
     * Approve expense (PENDING -> APPROVED)
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExpenseDTO>> approveExpense(@PathVariable Long id) {
        ExpenseDTO expense = expenseService.approveExpense(id);
        return ResponseEntity.ok(
                ApiResponse.success("Expense approved successfully", expense)
        );
    }

    /**
     * PUT /api/expenses/{id}/reject
     * Reject expense (PENDING -> REJECTED)
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExpenseDTO>> rejectExpense(@PathVariable Long id) {
        ExpenseDTO expense = expenseService.rejectExpense(id);
        return ResponseEntity.ok(
                ApiResponse.success("Expense rejected successfully", expense)
        );
    }

    /**
     * PUT /api/expenses/{id}/paid
     * Mark expense as paid (APPROVED -> PAID)
     */
    @PutMapping("/{id}/paid")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExpenseDTO>> markExpenseAsPaid(@PathVariable Long id) {
        ExpenseDTO expense = expenseService.markExpenseAsPaid(id);
        return ResponseEntity.ok(
                ApiResponse.success("Expense marked as paid successfully", expense)
        );
    }

    /**
     * DELETE /api/expenses/{id}
     * Delete expense (only PENDING or REJECTED can be deleted)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(
                ApiResponse.success("Expense deleted successfully", null)
        );
    }

}