package com.erp.enterprise.controller.inventory;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.inventory.PurchaseOrderCreateRequest;
import com.erp.enterprise.dto.inventory.PurchaseOrderDTO;
import com.erp.enterprise.service.inventory.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Purchase Order Controller
 *
 * Base URL: /api/purchase-orders
 */
@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @Autowired
    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> getAllPurchaseOrders() {
        List<PurchaseOrderDTO> purchaseOrders = purchaseOrderService.getAllPurchaseOrders();
        return ResponseEntity.ok(
                ApiResponse.success("Purchase orders retrieved successfully", purchaseOrders)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> getPurchaseOrderById(@PathVariable Long id) {
        PurchaseOrderDTO purchaseOrder = purchaseOrderService.getPurchaseOrderById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Purchase order retrieved successfully", purchaseOrder)
        );
    }

    @GetMapping("/number/{poNumber}")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> getPurchaseOrderByNumber(
            @PathVariable String poNumber) {

        PurchaseOrderDTO purchaseOrder = purchaseOrderService.getPurchaseOrderByNumber(poNumber);
        return ResponseEntity.ok(
                ApiResponse.success("Purchase order retrieved successfully", purchaseOrder)
        );
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> getPurchaseOrdersBySupplier(
            @PathVariable Long supplierId) {

        List<PurchaseOrderDTO> purchaseOrders =
                purchaseOrderService.getPurchaseOrdersBySupplier(supplierId);
        return ResponseEntity.ok(
                ApiResponse.success("Supplier purchase orders retrieved successfully", purchaseOrders)
        );
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> getPurchaseOrdersByWarehouse(
            @PathVariable Long warehouseId) {

        List<PurchaseOrderDTO> purchaseOrders =
                purchaseOrderService.getPurchaseOrdersByWarehouse(warehouseId);
        return ResponseEntity.ok(
                ApiResponse.success("Warehouse purchase orders retrieved successfully", purchaseOrders)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> getPurchaseOrdersByStatus(
            @PathVariable String status) {

        List<PurchaseOrderDTO> purchaseOrders =
                purchaseOrderService.getPurchaseOrdersByStatus(status);
        return ResponseEntity.ok(
                ApiResponse.success("Purchase orders by status retrieved successfully", purchaseOrders)
        );
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> getPurchaseOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<PurchaseOrderDTO> purchaseOrders =
                purchaseOrderService.getPurchaseOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success("Purchase orders in date range retrieved successfully", purchaseOrders)
        );
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> getRecentPurchaseOrders() {
        List<PurchaseOrderDTO> purchaseOrders = purchaseOrderService.getRecentPurchaseOrders();
        return ResponseEntity.ok(
                ApiResponse.success("Recent purchase orders retrieved successfully", purchaseOrders)
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderCreateRequest request) {

        PurchaseOrderDTO created = purchaseOrderService.createPurchaseOrder(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase order created successfully", created));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> approvePurchaseOrder(@PathVariable Long id) {
        PurchaseOrderDTO purchaseOrder = purchaseOrderService.approvePurchaseOrder(id);
        return ResponseEntity.ok(
                ApiResponse.success("Purchase order approved successfully", purchaseOrder)
        );
    }

    @PutMapping("/{id}/receive")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> receivePurchaseOrder(@PathVariable Long id) {
        PurchaseOrderDTO purchaseOrder = purchaseOrderService.receivePurchaseOrder(id);
        return ResponseEntity.ok(
                ApiResponse.success("Purchase order received successfully. Stock updated.", purchaseOrder)
        );
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> cancelPurchaseOrder(@PathVariable Long id) {
        PurchaseOrderDTO purchaseOrder = purchaseOrderService.cancelPurchaseOrder(id);
        return ResponseEntity.ok(
                ApiResponse.success("Purchase order cancelled successfully", purchaseOrder)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePurchaseOrder(@PathVariable Long id) {
        purchaseOrderService.deletePurchaseOrder(id);
        return ResponseEntity.ok(
                ApiResponse.success("Purchase order deleted successfully", null)
        );
    }
}