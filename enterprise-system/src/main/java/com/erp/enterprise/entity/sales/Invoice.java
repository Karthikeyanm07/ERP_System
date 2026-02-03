package com.erp.enterprise.entity.sales;

import com.erp.enterprise.entity.hr.BaseEntity;
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
 * Invoice Entity
 *
 * Business Logic:
 * - Billing document for sales orders
 * - Tracks payment status
 * - Status: UNPAID, PARTIAL, PAID, OVERDUE
 * - Links to customer and optionally to sales order
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {

    @NotBlank(message = "Invoice number is required")
    @Size(max = 30, message = "Invoice number must not exceed 30 characters")
    @Column(name = "invoice_number", unique = true, nullable = false, length = 30)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SalesOrder salesOrder;

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Customer customer;

    @NotNull(message = "Invoice date is required")
    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.0", message = "Subtotal cannot be negative")
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Tax amount cannot be negative")
    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", message = "Total amount cannot be negative")
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Paid amount cannot be negative")
    @Column(name = "paid_amount", precision = 15, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "status", length = 20)
    private String status = "UNPAID";  // UNPAID, PARTIAL, PAID, OVERDUE

    // Calculate remaining amount
    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(paidAmount);
    }

    // Update status based on payment
    public void updateStatus() {
        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.status = "UNPAID";
        } else if (paidAmount.compareTo(totalAmount) >= 0) {
            this.status = "PAID";
        } else {
            this.status = "PARTIAL";
        }

        // Check if overdue
        if (!"PAID".equals(this.status) && LocalDate.now().isAfter(this.dueDate)) {
            this.status = "OVERDUE";
        }
    }
}