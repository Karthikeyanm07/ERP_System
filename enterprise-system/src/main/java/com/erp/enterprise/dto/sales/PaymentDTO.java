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
public class PaymentDTO extends BaseDTO {

    @NotBlank(message = "Payment number is required")
    @Size(max = 30)
    private String paymentNumber;

    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;

    private String invoiceNumber;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;

    @Size(max = 30)
    private String paymentMethod;  // CASH, CARD, BANK_TRANSFER, CHEQUE

    @Size(max = 50)
    private String referenceNumber;

    private String remarks;

    private Long createdById;
    private String createdByName;
}