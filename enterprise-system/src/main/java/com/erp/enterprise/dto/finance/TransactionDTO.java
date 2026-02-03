package com.erp.enterprise.dto.finance;

import com.erp.enterprise.dto.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO extends BaseDTO {

    @NotBlank(message = "Transaction code is required")
    @Size(max = 30, message = "Transaction code must not exceed 30 characters")
    private String transactionCode;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    private String description;

    @Size(max = 50, message = "Reference number must not exceed 50 characters")
    private String referenceNumber;

    private Long createdById;
    private String createdByName;

    private List<TransactionEntryDTO> entries = new ArrayList<>();
}