package com.erp.enterprise.service.inventory;

import com.erp.enterprise.dto.inventory.StockMovementDTO;

import java.time.LocalDate;
import java.util.List;

public interface StockMovementService {

    StockMovementDTO getStockMovementById(Long id);
    List<StockMovementDTO> getAllStockMovements();
    List<StockMovementDTO> getStockMovementsByProduct(Long productId);
    List<StockMovementDTO> getStockMovementsByWarehouse(Long warehouseId);
    List<StockMovementDTO> getStockMovementsByType(String movementType);
    List<StockMovementDTO> getStockMovementsByProductAndWarehouse(Long productId, Long warehouseId);
    List<StockMovementDTO> getStockMovementsByDateRange(LocalDate startDate, LocalDate endDate);

    // Create stock movement (internal use by other services)
    StockMovementDTO createStockMovement(
            Long productId,
            Long warehouseId,
            String movementType,
            Integer quantity,
            String referenceType,
            Long referenceId,
            String remarks,
            Long createdById
    );
}