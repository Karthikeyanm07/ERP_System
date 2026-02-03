package com.erp.enterprise.service.inventory;

import com.erp.enterprise.dto.inventory.StockDTO;

import java.util.List;

/**
 * Stock Service Interface
 *
 * Critical Service: Manages inventory levels
 */
public interface StockService {

    // Get stock by ID
    StockDTO getStockById(Long id);

    // Get stock for product in warehouse
    StockDTO getStockByProductAndWarehouse(Long productId, Long warehouseId);

    // Get all stock for a product (across all warehouses)
    List<StockDTO> getStockByProduct(Long productId);

    // Get all stock in a warehouse
    List<StockDTO> getStockByWarehouse(Long warehouseId);

    // Get all stock
    List<StockDTO> getAllStock();

    // Get low stock items
    List<StockDTO> getLowStockItems();

    // Update stock quantity (internal use - called by movements)
    void updateStock(Long productId, Long warehouseId, Integer quantityChange);

    // Initialize stock for a product in a warehouse
    StockDTO initializeStock(Long productId, Long warehouseId);
}