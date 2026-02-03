package com.erp.enterprise.dto.sales;

import com.erp.enterprise.dto.BaseDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO extends BaseDTO {

    @NotBlank(message = "Invoice number is required")
    @Size(max = 30)
    private String invoiceNumber;

    private Long salesOrderId;
    private String salesOrderNumber;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private String customerName;

    @NotNull(message = "Invoice date is required")
    private LocalDate invoiceDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.0")
    private BigDecimal subtotal;

    @DecimalMin(value = "0.0")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.0")
    private BigDecimal discountAmount;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0")
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.0")
    private BigDecimal paidAmount;

    private String status;  // UNPAID, PARTIAL, PAID, OVERDUE

    // Calculated field
    private BigDecimal remainingAmount;
}