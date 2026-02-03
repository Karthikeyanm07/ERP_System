package com.erp.enterprise.controller.inventory;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.inventory.SupplierDTO;
import com.erp.enterprise.service.inventory.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Supplier Controller
 *
 * Base URL: /api/suppliers
 */
@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @Autowired
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierDTO>>> getAllSuppliers() {
        List<SupplierDTO> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(
                ApiResponse.success("Suppliers retrieved successfully", suppliers)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierDTO>> getSupplierById(@PathVariable Long id) {
        SupplierDTO supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Supplier retrieved successfully", supplier)
        );
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<SupplierDTO>> getSupplierByCode(@PathVariable String code) {
        SupplierDTO supplier = supplierService.getSupplierByCode(code);
        return ResponseEntity.ok(
                ApiResponse.success("Supplier retrieved successfully", supplier)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<SupplierDTO>>> getActiveSuppliers() {
        List<SupplierDTO> suppliers = supplierService.getActiveSuppliers();
        return ResponseEntity.ok(
                ApiResponse.success("Active suppliers retrieved successfully", suppliers)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SupplierDTO>>> searchSuppliers(
            @RequestParam(required = false) String keyword) {

        List<SupplierDTO> suppliers = supplierService.searchSuppliers(keyword);
        return ResponseEntity.ok(
                ApiResponse.success("Search completed successfully", suppliers)
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierDTO>> createSupplier(
            @Valid @RequestBody SupplierDTO supplierDTO) {

        SupplierDTO created = supplierService.createSupplier(supplierDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Supplier created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierDTO>> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierDTO supplierDTO) {

        SupplierDTO updated = supplierService.updateSupplier(id, supplierDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Supplier updated successfully", updated)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(
                ApiResponse.success("Supplier deleted successfully", null)
        );
    }
}