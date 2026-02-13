package com.erp.enterprise.service.finance;

import com.erp.enterprise.dto.finance.ExpenseCreateRequest;
import com.erp.enterprise.dto.finance.ExpenseDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Expense Service Interface
 */
public interface ExpenseService {

    // Create expense
    ExpenseDTO createExpense(@org.springframework.lang.NonNull ExpenseCreateRequest request);

    // Get expense by ID
    ExpenseDTO getExpenseById(@org.springframework.lang.NonNull Long id);

    // Get expense by code
    ExpenseDTO getExpenseByCode(@org.springframework.lang.NonNull String expenseCode);

    // Get all expenses
    List<ExpenseDTO> getAllExpenses();

    // Get expenses by employee
    List<ExpenseDTO> getExpensesByEmployee(@org.springframework.lang.NonNull Long employeeId);

    // Get expenses by status
    List<ExpenseDTO> getExpensesByStatus(@org.springframework.lang.NonNull String status);

    // Get expenses by category
    List<ExpenseDTO> getExpensesByCategory(@org.springframework.lang.NonNull String category);

    // Get expenses in date range
    List<ExpenseDTO> getExpensesByDateRange(@org.springframework.lang.NonNull LocalDate startDate, @org.springframework.lang.NonNull LocalDate endDate);

    // Get expenses by employee and status
    List<ExpenseDTO> getExpensesByEmployeeAndStatus(@org.springframework.lang.NonNull Long employeeId, @org.springframework.lang.NonNull String status);

    // Update expense
    ExpenseDTO updateExpense(@org.springframework.lang.NonNull Long id, @org.springframework.lang.NonNull ExpenseDTO expenseDTO);

    // Approve expense
    ExpenseDTO approveExpense(@org.springframework.lang.NonNull Long id);

    // Reject expense
    ExpenseDTO rejectExpense(@org.springframework.lang.NonNull Long id);

    // Mark expense as paid
    @org.springframework.lang.NonNull ExpenseDTO markExpenseAsPaid(@org.springframework.lang.NonNull Long id);

    // Delete expense
    void deleteExpense(@org.springframework.lang.NonNull Long id);
}