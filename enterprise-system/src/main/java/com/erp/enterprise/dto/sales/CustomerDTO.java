package com.erp.enterprise.dto.sales;

import com.erp.enterprise.dto.BaseDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
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
public class CustomerDTO extends BaseDTO {

    @NotBlank(message = "Customer code is required")
    @Size(max = 20)
    private String customerCode;

    @NotBlank(message = "Customer name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String contactPerson;

    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 20)
    private String phone;

    private String address;

    @Size(max = 50)
    private String city;

    @Size(max = 50)
    private String country;

    @DecimalMin(value = "0.0")
    private BigDecimal creditLimit;

    private BigDecimal outstandingBalance;

    private Boolean isActive;

    // Calculated field - available credit
    public BigDecimal getAvailableCredit() {
        if (creditLimit == null || outstandingBalance == null) {
            return BigDecimal.ZERO;
        }
        return creditLimit.subtract(outstandingBalance);
    }
}