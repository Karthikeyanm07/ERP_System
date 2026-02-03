package com.erp.enterprise.controller.inventory;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.inventory.StockMovementDTO;
import com.erp.enterprise.service.inventory.StockMovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Stock Movement Controller
 *
 * Base URL: /api/stock-movements
 */
@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @Autowired
    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockMovementDTO>>> getAllStockMovements() {
        List<StockMovementDTO> movements = stockMovementService.getAllStockMovements();
        return ResponseEntity.ok(
                ApiResponse.success("Stock movements retrieved successfully", movements)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockMovementDTO>> getStockMovementById(@PathVariable Long id) {
        StockMovementDTO movement = stockMovementService.getStockMovementById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Stock movement retrieved successfully", movement)
        );
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<StockMovementDTO>>> getStockMovementsByProduct(
            @PathVariable Long productId) {

        List<StockMovementDTO> movements = stockMovementService.getStockMovementsByProduct(productId);
        return ResponseEntity.ok(
                ApiResponse.success("Product stock movements retrieved successfully", movements)
        );
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<ApiResponse<List<StockMovementDTO>>> getStockMovementsByWarehouse(
            @PathVariable Long warehouseId) {

        List<StockMovementDTO> movements =
                stockMovementService.getStockMovementsByWarehouse(warehouseId);
        return ResponseEntity.ok(
                ApiResponse.success("Warehouse stock movements retrieved successfully", movements)
        );
    }

    @GetMapping("/type/{movementType}")
    public ResponseEntity<ApiResponse<List<StockMovementDTO>>> getStockMovementsByType(
            @PathVariable String movementType) {

        List<StockMovementDTO> movements = stockMovementService.getStockMovementsByType(movementType);
        return ResponseEntity.ok(
                ApiResponse.success("Stock movements by type retrieved successfully", movements)
        );
    }

    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    public ResponseEntity<ApiResponse<List<StockMovementDTO>>> getStockMovementsByProductAndWarehouse(
            @PathVariable Long productId,
            @PathVariable Long warehouseId) {

        List<StockMovementDTO> movements =
                stockMovementService.getStockMovementsByProductAndWarehouse(productId, warehouseId);
        return ResponseEntity.ok(
                ApiResponse.success("Stock movements retrieved successfully", movements)
        );
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<StockMovementDTO>>> getStockMovementsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<StockMovementDTO> movements =
                stockMovementService.getStockMovementsByDateRange(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success("Stock movements in date range retrieved successfully", movements)
        );
    }
}