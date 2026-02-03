package com.erp.enterprise.entity.finance;

import com.erp.enterprise.entity.hr.BaseEntity;
import com.erp.enterprise.entity.hr.Employee;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Expense Entity
 *
 * Business Logic:
 * - Tracks expenses incurred by employees
 * - Categories: Travel, Office Supplies, Entertainment, etc.
 * - Status: PENDING, APPROVED, PAID
 * - Approved expenses can be reimbursed to employees
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "expenses")
public class Expense extends BaseEntity {

    @NotBlank(message = "Expense code is required")
    @Size(max = 30, message = "Expense code must not exceed 30 characters")
    @Column(name = "expense_code", unique = true, nullable = false, length = 30)
    private String expenseCode;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    @Column(name = "category", nullable = false, length = 50)
    private String category;  // TRAVEL, OFFICE_SUPPLIES, ENTERTAINMENT, UTILITIES, etc.

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Expense date is required")
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Size(max = 100, message = "Vendor name must not exceed 100 characters")
    @Column(name = "vendor_name", length = 100)
    private String vendorName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Employee employee;

    @Column(name = "status", length = 20)
    private String status = "PENDING";  // PENDING, APPROVED, PAID
}