package com.erp.enterprise.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO {

    private Long id;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productCode;
    private String productName;
    private String unit;
    private BigDecimal unitPrice;
    private Integer reorderLevel;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    private String warehouseName;

    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    private LocalDateTime lastUpdated;
}