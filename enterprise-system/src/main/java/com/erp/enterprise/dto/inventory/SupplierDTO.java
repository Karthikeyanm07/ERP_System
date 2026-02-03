package com.erp.enterprise.dto.inventory;

import com.erp.enterprise.dto.BaseDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO extends BaseDTO {

    @NotBlank(message = "Supplier code is required")
    @Size(max = 20)
    private String supplierCode;

    @NotBlank(message = "Supplier name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String contactPerson;

    @Email(message = "Email should be valid")
    private String email;

    @Pattern(regexp = "^[0-9]{10,20}$", message = "Phone number should be valid")
    private String phone;

    private String address;

    @Size(max = 50)
    private String city;

    @Size(max = 50)
    private String country;

    private Boolean isActive;
}