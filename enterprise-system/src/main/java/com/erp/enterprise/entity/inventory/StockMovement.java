package com.erp.enterprise.entity.inventory;

import com.erp.enterprise.entity.hr.BaseEntity;
import com.erp.enterprise.entity.hr.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Stock Movement Entity
 *
 * Business Logic:
 * - Complete audit trail of all inventory changes
 * - Types: IN (receipt), OUT (issue), TRANSFER, ADJUSTMENT
 * - Links to source transactions (PO, Sales Order, etc.)
 * - Immutable - never deleted, only new records added
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_movements")
public class StockMovement extends BaseEntity {

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @NotNull(message = "Warehouse is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Warehouse warehouse;

    @NotBlank(message = "Movement type is required")
    @Column(name = "movement_type", nullable = false, length = 20)
    private String movementType;  // IN, OUT, TRANSFER, ADJUSTMENT

    @NotNull(message = "Quantity is required")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reference_type", length = 50)
    private String referenceType;  // PURCHASE_ORDER, SALES_ORDER, ADJUSTMENT

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User createdBy;
}