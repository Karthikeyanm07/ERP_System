package com.erp.enterprise.service.finance.impl;

import com.erp.enterprise.dto.finance.ExpenseCreateRequest;
import com.erp.enterprise.dto.finance.ExpenseDTO;
import com.erp.enterprise.entity.hr.Employee;
import com.erp.enterprise.entity.finance.Expense;
import com.erp.enterprise.exception.BusinessException;
import com.erp.enterprise.exception.DuplicateResourceException;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.hr.EmployeeRepository;
import com.erp.enterprise.repository.finanace.ExpenseRepository;
import com.erp.enterprise.service.finance.ExpenseService;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Expense Service Implementation
 *
 * Business Logic:
 * - Employees submit expenses for reimbursement
 * - Expenses flow: PENDING → APPROVED → PAID
 * - Tracks categories and vendors
 * - Validates status transitions
 */
@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository,
                              EmployeeRepository employeeRepository) {
        this.expenseRepository = expenseRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public ExpenseDTO createExpense(ExpenseCreateRequest request) {
        // Business Logic: Check if expense code exists
        if (expenseRepository.existsByExpenseCode(request.getExpenseCode())) {
            throw new DuplicateResourceException(
                    "Expense", "expenseCode", request.getExpenseCode());
        }

        // Create expense
        Expense expense = new Expense();
        expense.setExpenseCode(request.getExpenseCode());
        expense.setCategory(request.getCategory());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setVendorName(request.getVendorName());
        expense.setDescription(request.getDescription());
        expense.setStatus("PENDING");  // New expenses are pending

        // Set employee if provided
        if (request.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee", "id", request.getEmployeeId()));
            expense.setEmployee(employee);
        }

        Expense savedExpense = expenseRepository.save(expense);
        return DtoMapper.toExpenseDTO(savedExpense);
    }

    @Override
    public ExpenseDTO getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        return DtoMapper.toExpenseDTO(expense);
    }

    @Override
    public ExpenseDTO getExpenseByCode(String expenseCode) {
        Expense expense = expenseRepository.findByExpenseCode(expenseCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expense", "expenseCode", expenseCode));

        return DtoMapper.toExpenseDTO(expense);
    }

    @Override
    public List<ExpenseDTO> getAllExpenses() {
        List<Expense> expenses = expenseRepository.findAll();

        return expenses.stream()
                .map(DtoMapper::toExpenseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDTO> getExpensesByEmployee(Long employeeId) {
        // Validate employee exists
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        List<Expense> expenses = expenseRepository.findByEmployeeIdOrderByExpenseDateDesc(employeeId);

        return expenses.stream()
                .map(DtoMapper::toExpenseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDTO> getExpensesByStatus(String status) {
        // Validate status
        if (!isValidExpenseStatus(status)) {
            throw new BusinessException(
                    "Invalid expense status: " + status,
                    "INVALID_EXPENSE_STATUS");
        }

        List<Expense> expenses = expenseRepository.findByStatusOrderByExpenseDateDesc(status);

        return expenses.stream()
                .map(DtoMapper::toExpenseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDTO> getExpensesByCategory(String category) {
        List<Expense> expenses = expenseRepository.findByCategoryOrderByExpenseDateDesc(category);

        return expenses.stream()
                .map(DtoMapper::toExpenseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDTO> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(
                    "Start date cannot be after end date",
                    "INVALID_DATE_RANGE");
        }

        List<Expense> expenses = expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDesc(
                startDate, endDate);

        return expenses.stream()
                .map(DtoMapper::toExpenseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDTO> getExpensesByEmployeeAndStatus(Long employeeId, String status) {
        // Validate employee exists
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        // Validate status
        if (!isValidExpenseStatus(status)) {
            throw new BusinessException(
                    "Invalid expense status: " + status,
                    "INVALID_EXPENSE_STATUS");
        }

        List<Expense> expenses = expenseRepository.findByEmployeeIdAndStatusOrderByExpenseDateDesc(
                employeeId, status);

        return expenses.stream()
                .map(DtoMapper::toExpenseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ExpenseDTO updateExpense(Long id, ExpenseDTO expenseDTO) {
        // Find existing expense
        Expense existingExpense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        // Business Logic: Can only update pending expenses
        if (!"PENDING".equals(existingExpense.getStatus())) {
            throw new BusinessException(
                    "Can only update pending expenses",
                    "CANNOT_UPDATE_PROCESSED_EXPENSE");
        }

        // Business Logic: Check if new code conflicts
        if (!existingExpense.getExpenseCode().equals(expenseDTO.getExpenseCode()) &&
                expenseRepository.existsByExpenseCode(expenseDTO.getExpenseCode())) {
            throw new DuplicateResourceException(
                    "Expense", "expenseCode", expenseDTO.getExpenseCode());
        }

        // Update fields
        existingExpense.setExpenseCode(expenseDTO.getExpenseCode());
        existingExpense.setCategory(expenseDTO.getCategory());
        existingExpense.setAmount(expenseDTO.getAmount());
        existingExpense.setExpenseDate(expenseDTO.getExpenseDate());
        existingExpense.setVendorName(expenseDTO.getVendorName());
        existingExpense.setDescription(expenseDTO.getDescription());

        // Update employee if provided
        if (expenseDTO.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(expenseDTO.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee", "id", expenseDTO.getEmployeeId()));
            existingExpense.setEmployee(employee);
        }

        Expense updatedExpense = expenseRepository.save(existingExpense);
        return DtoMapper.toExpenseDTO(updatedExpense);
    }

    @Override
    public ExpenseDTO approveExpense(Long id) {
        // Find expense
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        // Business Logic: Can only approve pending expenses
        if (!"PENDING".equals(expense.getStatus())) {
            throw new BusinessException(
                    "Can only approve pending expenses. Current status: " + expense.getStatus(),
                    "INVALID_STATUS_TRANSITION");
        }

        // Approve expense
        expense.setStatus("APPROVED");

        Expense updatedExpense = expenseRepository.save(expense);
        return DtoMapper.toExpenseDTO(updatedExpense);
    }

    @Override
    public ExpenseDTO rejectExpense(Long id) {
        // Find expense
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        // Business Logic: Can only reject pending expenses
        if (!"PENDING".equals(expense.getStatus())) {
            throw new BusinessException(
                    "Can only reject pending expenses. Current status: " + expense.getStatus(),
                    "INVALID_STATUS_TRANSITION");
        }

        // Reject expense (set back to pending with a flag, or create a REJECTED status)
        expense.setStatus("REJECTED");

        Expense updatedExpense = expenseRepository.save(expense);
        return DtoMapper.toExpenseDTO(updatedExpense);
    }

    @Override
    public ExpenseDTO markExpenseAsPaid(Long id) {
        // Find expense
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        // Business Logic: Can only mark approved expenses as paid
        if (!"APPROVED".equals(expense.getStatus())) {
            throw new BusinessException(
                    "Can only mark approved expenses as paid. Current status: " + expense.getStatus(),
                    "INVALID_STATUS_TRANSITION");
        }

        // Mark as paid
        expense.setStatus("PAID");

        Expense updatedExpense = expenseRepository.save(expense);
        return DtoMapper.toExpenseDTO(updatedExpense);
    }

    @Override
    public void deleteExpense(Long id) {
        // Find expense
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        // Business Logic: Can only delete pending or rejected expenses
        if ("APPROVED".equals(expense.getStatus()) || "PAID".equals(expense.getStatus())) {
            throw new BusinessException(
                    "Cannot delete approved or paid expenses",
                    "CANNOT_DELETE_PROCESSED_EXPENSE");
        }

        expenseRepository.delete(expense);
    }

    // Helper method to validate expense status
    private boolean isValidExpenseStatus(String status) {
        return status != null &&
                (status.equals("PENDING") ||
                        status.equals("APPROVED") ||
                        status.equals("REJECTED") ||
                        status.equals("PAID"));
    }
}