package com.erp.enterprise.controller.inventory;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.inventory.ProductDTO;
import com.erp.enterprise.service.inventory.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Product Controller
 *
 * Base URL: /api/products
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(
                ApiResponse.success("Products retrieved successfully", products)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Product retrieved successfully", product)
        );
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductByCode(@PathVariable String code) {
        ProductDTO product = productService.getProductByCode(code);
        return ResponseEntity.ok(
                ApiResponse.success("Product retrieved successfully", product)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getActiveProducts() {
        List<ProductDTO> products = productService.getActiveProducts();
        return ResponseEntity.ok(
                ApiResponse.success("Active products retrieved successfully", products)
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(
            @PathVariable Long categoryId) {

        List<ProductDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(
                ApiResponse.success("Products by category retrieved successfully", products)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(
            @RequestParam(required = false) String keyword) {

        List<ProductDTO> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(
                ApiResponse.success("Search completed successfully", products)
        );
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsBelowReorderLevel() {
        List<ProductDTO> products = productService.getProductsBelowReorderLevel();
        return ResponseEntity.ok(
                ApiResponse.success("Low stock products retrieved successfully", products)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('WAREHOUSE_STAFF') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @Valid @RequestBody ProductDTO productDTO) {

        ProductDTO created = productService.createProduct(productDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('WAREHOUSE_STAFF') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {

        ProductDTO updated = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Product updated successfully", updated)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.success("Product deleted successfully", null)
        );
    }
}