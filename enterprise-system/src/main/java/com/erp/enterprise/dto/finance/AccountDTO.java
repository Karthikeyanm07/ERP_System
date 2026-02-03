package com.erp.enterprise.dto.finance;

import com.erp.enterprise.dto.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO extends BaseDTO {

    @NotBlank(message = "Account code is required")
    @Size(max = 20, message = "Account code must not exceed 20 characters")
    private String accountCode;

    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String accountName;

    @NotBlank(message = "Account type is required")
    private String accountType;  // ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE

    private Long parentAccountId;
    private String parentAccountName;

    private BigDecimal balance;
    private Boolean isActive;
}