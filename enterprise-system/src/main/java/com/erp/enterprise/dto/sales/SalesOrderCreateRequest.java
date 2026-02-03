package com.erp.enterprise.dto.sales;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderCreateRequest {

    @NotBlank(message = "Order number is required")
    @Size(max = 30)
    private String orderNumber;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotNull(message = "Order date is required")
    private LocalDate orderDate;

    private LocalDate deliveryDate;

    @DecimalMin(value = "0.0")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.0")
    private BigDecimal discountAmount;

    @NotNull(message = "Created by user ID is required")
    private Long createdById;

    @NotEmpty(message = "Sales order must have at least one item")
    @Valid
    private List<SalesOrderItemDTO> items;
}