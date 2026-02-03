package com.erp.enterprise.dto.finance;

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
public class ExpenseDTO extends BaseDTO {

    @NotBlank(message = "Expense code is required")
    @Size(max = 30)
    private String expenseCode;

    @NotBlank(message = "Category is required")
    @Size(max = 50)
    private String category;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    @Size(max = 100)
    private String vendorName;

    private String description;

    private Long employeeId;
    private String employeeCode;
    private String employeeName;

    private String status;  // PENDING, APPROVED, PAID
}