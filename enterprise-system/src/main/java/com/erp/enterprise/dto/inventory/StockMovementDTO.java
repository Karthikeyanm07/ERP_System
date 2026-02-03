package com.erp.enterprise.dto.inventory;

import com.erp.enterprise.dto.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementDTO extends BaseDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productCode;
    private String productName;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    private String warehouseName;

    @NotBlank(message = "Movement type is required")
    private String movementType;  // IN, OUT, TRANSFER, ADJUSTMENT

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    private String referenceType;
    private Long referenceId;
    private String remarks;

    private Long createdById;
    private String createdByName;
}