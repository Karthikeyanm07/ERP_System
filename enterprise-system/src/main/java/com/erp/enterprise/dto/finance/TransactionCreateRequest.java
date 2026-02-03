package com.erp.enterprise.dto.finance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreateRequest {

    @NotBlank(message = "Transaction code is required")
    @Size(max = 30)
    private String transactionCode;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    private String description;
    private String referenceNumber;

    @NotNull(message = "Created by user ID is required")
    private Long createdById;

    @NotEmpty(message = "Transaction must have at least one entry")
    @Valid
    private List<TransactionEntryDTO> entries;
}