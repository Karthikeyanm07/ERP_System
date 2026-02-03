package com.erp.enterprise.dto.sales;

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
public class InvoiceCreateRequest {

    @NotBlank(message = "Invoice number is required")
    @Size(max = 30)
    private String invoiceNumber;

    private Long salesOrderId;  // Optional - can create invoice without sales order

    @NotNull(message = "Customer ID is required")
    private Long customerId;

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
}