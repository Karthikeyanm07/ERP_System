package com.erp.enterprise.entity.sales;

import com.erp.enterprise.entity.hr.BaseEntity;
import com.erp.enterprise.entity.hr.User;
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
 * Payment Entity
 *
 * Business Logic:
 * - Payment receipts from customers
 * - Links to invoice
 * - Multiple payments can be made against one invoice
 * - Tracks payment method and reference
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @NotBlank(message = "Payment number is required")
    @Size(max = 30, message = "Payment number must not exceed 30 characters")
    @Column(name = "payment_number", unique = true, nullable = false, length = 30)
    private String paymentNumber;

    @NotNull(message = "Invoice is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Invoice invoice;

    @NotNull(message = "Payment date is required")
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Size(max = 30, message = "Payment method must not exceed 30 characters")
    @Column(name = "payment_method", length = 30)
    private String paymentMethod;  // CASH, CARD, BANK_TRANSFER, CHEQUE

    @Size(max = 50, message = "Reference number must not exceed 50 characters")
    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User createdBy;
}