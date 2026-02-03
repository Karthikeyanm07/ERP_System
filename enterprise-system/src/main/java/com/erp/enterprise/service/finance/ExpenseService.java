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
    ExpenseDTO createExpense(ExpenseCreateRequest request);

    // Get expense by ID
    ExpenseDTO getExpenseById(Long id);

    // Get expense by code
    ExpenseDTO getExpenseByCode(String expenseCode);

    // Get all expenses
    List<ExpenseDTO> getAllExpenses();

    // Get expenses by employee
    List<ExpenseDTO> getExpensesByEmployee(Long employeeId);

    // Get expenses by status
    List<ExpenseDTO> getExpensesByStatus(String status);

    // Get expenses by category
    List<ExpenseDTO> getExpensesByCategory(String category);

    // Get expenses in date range
    List<ExpenseDTO> getExpensesByDateRange(LocalDate startDate, LocalDate endDate);

    // Get expenses by employee and status
    List<ExpenseDTO> getExpensesByEmployeeAndStatus(Long employeeId, String status);

    // Update expense
    ExpenseDTO updateExpense(Long id, ExpenseDTO expenseDTO);

    // Approve expense
    ExpenseDTO approveExpense(Long id);

    // Reject expense
    ExpenseDTO rejectExpense(Long id);

    // Mark expense as paid
    ExpenseDTO markExpenseAsPaid(Long id);

    // Delete expense
    void deleteExpense(Long id);
}