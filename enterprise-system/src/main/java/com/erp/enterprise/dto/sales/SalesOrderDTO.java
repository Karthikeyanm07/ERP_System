package com.erp.enterprise.dto.sales;

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
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderDTO extends BaseDTO {

    @NotBlank(message = "Order number is required")
    @Size(max = 30)
    private String orderNumber;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private String customerName;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    private String warehouseName;

    @NotNull(message = "Order date is required")
    private LocalDate orderDate;

    private LocalDate deliveryDate;

    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.0")
    private BigDecimal subtotal;

    @DecimalMin(value = "0.0")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.0")
    private BigDecimal discountAmount;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0")
    private BigDecimal totalAmount;

    private String status;  // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED

    private Long createdById;
    private String createdByName;

    private List<SalesOrderItemDTO> items = new ArrayList<>();
}