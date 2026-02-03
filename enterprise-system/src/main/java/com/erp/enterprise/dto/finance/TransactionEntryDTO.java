package com.erp.enterprise.dto.finance;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntryDTO {

    private Long id;

    @NotNull(message = "Account ID is required")
    private Long accountId;

    private String accountCode;
    private String accountName;

    @NotBlank(message = "Entry type is required")
    private String entryType;  // DEBIT or CREDIT

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String description;
}