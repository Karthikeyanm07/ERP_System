package com.erp.enterprise.dto.inventory;

import com.erp.enterprise.dto.BaseDTO;
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
public class PurchaseOrderDTO extends BaseDTO {

    @NotBlank(message = "PO number is required")
    @Size(max = 30)
    private String poNumber;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private String supplierName;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    private String warehouseName;

    @NotNull(message = "Order date is required")
    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    private BigDecimal totalAmount;

    private String status;  // PENDING, APPROVED, RECEIVED, CANCELLED

    private Long createdById;
    private String createdByName;

    private List<PurchaseOrderItemDTO> items = new ArrayList<>();
}