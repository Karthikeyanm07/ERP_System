package com.erp.enterprise.controller.inventory;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.inventory.StockDTO;
import com.erp.enterprise.service.inventory.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Stock Controller
 *
 * Base URL: /api/stock
 */
@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockDTO>>> getAllStock() {
        List<StockDTO> stock = stockService.getAllStock();
        return ResponseEntity.ok(
                ApiResponse.success("Stock retrieved successfully", stock)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockDTO>> getStockById(@PathVariable Long id) {
        StockDTO stock = stockService.getStockById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Stock retrieved successfully", stock)
        );
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<StockDTO>>> getStockByProduct(
            @PathVariable Long productId) {

        List<StockDTO> stock = stockService.getStockByProduct(productId);
        return ResponseEntity.ok(
                ApiResponse.success("Product stock retrieved successfully", stock)
        );
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<ApiResponse<List<StockDTO>>> getStockByWarehouse(
            @PathVariable Long warehouseId) {

        List<StockDTO> stock = stockService.getStockByWarehouse(warehouseId);
        return ResponseEntity.ok(
                ApiResponse.success("Warehouse stock retrieved successfully", stock)
        );
    }

    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    public ResponseEntity<ApiResponse<StockDTO>> getStockByProductAndWarehouse(
            @PathVariable Long productId,
            @PathVariable Long warehouseId) {

        StockDTO stock = stockService.getStockByProductAndWarehouse(productId, warehouseId);
        return ResponseEntity.ok(
                ApiResponse.success("Stock retrieved successfully", stock)
        );
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<StockDTO>>> getLowStockItems() {
        List<StockDTO> stock = stockService.getLowStockItems();
        return ResponseEntity.ok(
                ApiResponse.success("Low stock items retrieved successfully", stock)
        );
    }
}