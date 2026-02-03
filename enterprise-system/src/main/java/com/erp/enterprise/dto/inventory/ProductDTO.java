package com.erp.enterprise.dto.inventory;

import com.erp.enterprise.dto.BaseDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class ProductDTO extends BaseDTO {

    @NotBlank(message = "Product code is required")
    @Size(max = 30)
    private String productCode;

    @NotBlank(message = "Product name is required")
    @Size(max = 200)
    private String name;

    private String description;

    private Long categoryId;
    private String categoryName;

    @NotBlank(message = "Unit is required")
    @Size(max = 20)
    private String unit;

    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    @Min(value = 0, message = "Reorder level cannot be negative")
    private Integer reorderLevel;

    private Boolean isActive;

    private Integer totalStock;  // Calculated field - total across all warehouses
}