package com.erp.enterprise.dto.inventory;

import com.erp.enterprise.dto.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseDTO extends BaseDTO {

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String location;

    private Long managerId;
    private String managerName;

    private Boolean isActive;
}