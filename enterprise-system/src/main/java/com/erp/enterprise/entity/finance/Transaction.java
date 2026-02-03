package com.erp.enterprise.entity.finance;

import com.erp.enterprise.entity.hr.BaseEntity;
import com.erp.enterprise.entity.hr.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction Entity
 *
 * Business Logic:
 * - Represents a financial transaction with multiple entries
 * - Double-entry bookkeeping: Debits = Credits
 * - Each transaction has 2+ entries (debit and credit sides)
 *
 * Example:
 * Transaction: "Paid salary to employee"
 * - Entry 1: Debit "Salary Expense" 50,000
 * - Entry 2: Credit "Bank Account" 50,000
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {

    @NotBlank(message = "Transaction code is required")
    @Size(max = 30, message = "Transaction code must not exceed 30 characters")
    @Column(name = "transaction_code", unique = true, nullable = false, length = 30)
    private String transactionCode;

    @NotNull(message = "Transaction date is required")
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Size(max = 50, message = "Reference number must not exceed 50 characters")
    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User createdBy;

    // One transaction has multiple entries (debit & credit)
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionEntry> entries = new ArrayList<>();

    // Helper method to add entry
    public void addEntry(TransactionEntry entry) {
        entries.add(entry);
        entry.setTransaction(this);
    }

    // Helper method to remove entry
    public void removeEntry(TransactionEntry entry) {
        entries.remove(entry);
        entry.setTransaction(null);
    }
}