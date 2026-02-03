package com.erp.enterprise.controller.sales;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.sales.SalesOrderCreateRequest;
import com.erp.enterprise.dto.sales.SalesOrderDTO;
import com.erp.enterprise.service.sales.SalesOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Sales Order Controller
 *
 * Base URL: /api/sales-orders
 *
 * Available endpoints:
 * - GET    /api/sales-orders                      -> Get all sales orders
 * - GET    /api/sales-orders/{id}                 -> Get by ID
 * - GET    /api/sales-orders/number/{number}      -> Get by order number
 * - GET    /api/sales-orders/customer/{customerId} -> Get by customer
 * - GET    /api/sales-orders/warehouse/{warehouseId} -> Get by warehouse
 * - GET    /api/sales-orders/status/{status}      -> Get by status
 * - GET    /api/sales-orders/date-range           -> Get by date range
 * - GET    /api/sales-orders/recent               -> Get recent orders
 * - POST   /api/sales-orders                      -> Create sales order
 * - PUT    /api/sales-orders/{id}/confirm         -> Confirm order (reduce stock)
 * - PUT    /api/sales-orders/{id}/ship            -> Ship order
 * - PUT    /api/sales-orders/{id}/deliver         -> Deliver order
 * - PUT    /api/sales-orders/{id}/cancel          -> Cancel order
 * - DELETE /api/sales-orders/{id}                 -> Delete order
 */
@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @Autowired
    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SalesOrderDTO>>> getAllSalesOrders() {
        List<SalesOrderDTO> salesOrders = salesOrderService.getAllSalesOrders();
        return ResponseEntity.ok(
                ApiResponse.success("Sales orders retrieved successfully", salesOrders)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> getSalesOrderById(@PathVariable Long id) {
        SalesOrderDTO salesOrder = salesOrderService.getSalesOrderById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Sales order retrieved successfully", salesOrder)
        );
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> getSalesOrderByNumber(@PathVariable String number) {
        SalesOrderDTO salesOrder = salesOrderService.getSalesOrderByNumber(number);
        return ResponseEntity.ok(
                ApiResponse.success("Sales order retrieved successfully", salesOrder)
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<SalesOrderDTO>>> getSalesOrdersByCustomer(
            @PathVariable Long customerId) {

        List<SalesOrderDTO> salesOrders = salesOrderService.getSalesOrdersByCustomer(customerId);
        return ResponseEntity.ok(
                ApiResponse.success("Customer sales orders retrieved successfully", salesOrders)
        );
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<ApiResponse<List<SalesOrderDTO>>> getSalesOrdersByWarehouse(
            @PathVariable Long warehouseId) {

        List<SalesOrderDTO> salesOrders = salesOrderService.getSalesOrdersByWarehouse(warehouseId);
        return ResponseEntity.ok(
                ApiResponse.success("Warehouse sales orders retrieved successfully", salesOrders)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<SalesOrderDTO>>> getSalesOrdersByStatus(
            @PathVariable String status) {

        List<SalesOrderDTO> salesOrders = salesOrderService.getSalesOrdersByStatus(status);
        return ResponseEntity.ok(
                ApiResponse.success("Sales orders by status retrieved successfully", salesOrders)
        );
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<SalesOrderDTO>>> getSalesOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<SalesOrderDTO> salesOrders = salesOrderService.getSalesOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success("Sales orders in date range retrieved successfully", salesOrders)
        );
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<SalesOrderDTO>>> getRecentSalesOrders() {
        List<SalesOrderDTO> salesOrders = salesOrderService.getRecentSalesOrders();
        return ResponseEntity.ok(
                ApiResponse.success("Recent sales orders retrieved successfully", salesOrders)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('SALES_STAFF') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> createSalesOrder(
            @Valid @RequestBody SalesOrderCreateRequest request) {

        SalesOrderDTO created = salesOrderService.createSalesOrder(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sales order created successfully", created));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> confirmSalesOrder(@PathVariable Long id) {
        SalesOrderDTO salesOrder = salesOrderService.confirmSalesOrder(id);
        return ResponseEntity.ok(
                ApiResponse.success("Sales order confirmed successfully. Stock reduced.", salesOrder)
        );
    }

    @PutMapping("/{id}/ship")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> shipSalesOrder(@PathVariable Long id) {
        SalesOrderDTO salesOrder = salesOrderService.shipSalesOrder(id);
        return ResponseEntity.ok(
                ApiResponse.success("Sales order shipped successfully", salesOrder)
        );
    }

    @PutMapping("/{id}/deliver")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> deliverSalesOrder(@PathVariable Long id) {
        SalesOrderDTO salesOrder = salesOrderService.deliverSalesOrder(id);
        return ResponseEntity.ok(
                ApiResponse.success("Sales order delivered successfully", salesOrder)
        );
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> cancelSalesOrder(@PathVariable Long id) {
        SalesOrderDTO salesOrder = salesOrderService.cancelSalesOrder(id);
        return ResponseEntity.ok(
                ApiResponse.success("Sales order cancelled successfully", salesOrder)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSalesOrder(@PathVariable Long id) {
        salesOrderService.deleteSalesOrder(id);
        return ResponseEntity.ok(
                ApiResponse.success("Sales order deleted successfully", null)
        );
    }
}