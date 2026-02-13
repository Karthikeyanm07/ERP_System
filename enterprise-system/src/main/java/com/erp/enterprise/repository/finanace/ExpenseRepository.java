package com.erp.enterprise.repository.finanace;

import com.erp.enterprise.entity.finance.Expense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Expense Repository
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Override
    @EntityGraph(attributePaths = {"employee"})
    @org.springframework.lang.NonNull
    List<Expense> findAll();

    // Check if expense code exists
    boolean existsByExpenseCode(String expenseCode);

    // Find expense by code
    Optional<Expense> findByExpenseCode(String expenseCode);

    // Find expenses by employee
    List<Expense> findByEmployeeIdOrderByExpenseDateDesc(Long employeeId);

    // Find expenses by status
    List<Expense> findByStatusOrderByExpenseDateDesc(String status);

    // Find expenses by category
    List<Expense> findByCategoryOrderByExpenseDateDesc(String category);

    // Find expenses by date range
    List<Expense> findByExpenseDateBetweenOrderByExpenseDateDesc(
            LocalDate startDate, LocalDate endDate);

    // Find expenses by employee and status
    List<Expense> findByEmployeeIdAndStatusOrderByExpenseDateDesc(Long employeeId, String status);

    // Calculate total expenses by category
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.category = :category AND e.status = 'APPROVED'")
    BigDecimal sumApprovedExpensesByCategory(@Param("category") String category);

    // Calculate total expenses by employee
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.employee.id = :employeeId AND e.status = 'APPROVED'")
    BigDecimal sumApprovedExpensesByEmployee(@Param("employeeId") Long employeeId);

    // Get expenses for employee in date range
    @Query("SELECT e FROM Expense e " +
            "WHERE e.employee.id = :employeeId " +
            "AND e.expenseDate BETWEEN :startDate AND :endDate " +
            "ORDER BY e.expenseDate DESC")
    List<Expense> findByEmployeeAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}