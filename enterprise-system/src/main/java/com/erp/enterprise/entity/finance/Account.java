package com.erp.enterprise.entity.finance;

import com.erp.enterprise.entity.hr.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Account Entity - Chart of Accounts
 *
 * Business Logic:
 * - Represents all accounts in the organization
 * - Types: ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
 * - Hierarchical structure (parent-child relationships)
 * - Balance tracking
 *
 * Examples:
 * - Assets: Cash, Bank, Accounts Receivable
 * - Liabilities: Loans, Accounts Payable
 * - Revenue: Sales, Service Income
 * - Expenses: Salaries, Rent, Utilities
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {

    @NotBlank(message = "Account code is required")
    @Size(max = 20, message = "Account code must not exceed 20 characters")
    @Column(name = "account_code", unique = true, nullable = false, length = 20)
    private String accountCode;

    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;

    @NotBlank(message = "Account type is required")
    @Column(name = "account_type", nullable = false, length = 20)
    private String accountType;  // ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE

    // Parent account for hierarchical structure
    // Example: "Bank Accounts" can be parent of "HDFC Bank", "ICICI Bank"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_account_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Account parentAccount;

    @Column(name = "balance", precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public Account(String accountCode, String accountName, String accountType) {
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.accountType = accountType;
    }
}