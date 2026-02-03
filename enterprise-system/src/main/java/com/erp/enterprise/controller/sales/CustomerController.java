package com.erp.enterprise.controller.sales;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.sales.CustomerDTO;
import com.erp.enterprise.dto.sales.CustomerListResponse;
import com.erp.enterprise.dto.sales.CustomerDetailResponse;
import com.erp.enterprise.service.sales.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer Controller
 *
 * Base URL: /api/customers
 *
 * Available endpoints:
 * - GET    /api/customers                       -> Get all customers (list view, excludes sensitive fields)
 * - GET    /api/customers/{id}                  -> Get by ID (detail view, includes all fields, SALES/ADMIN only)
 * - GET    /api/customers/code/{code}           -> Get by code
 * - GET    /api/customers/active                -> Get active customers
 * - GET    /api/customers/search?keyword=xyz    -> Search customers
 * - GET    /api/customers/exceeding-credit      -> Get customers exceeding credit limit
 * - POST   /api/customers                       -> Create customer
 * - PUT    /api/customers/{id}                  -> Update customer
 * - DELETE /api/customers/{id}                  -> Delete customer
 * 
 * Security:
 * - List endpoint returns CustomerListResponse (excludes creditLimit, outstandingBalance)
 * - Detail endpoint returns CustomerDetailResponse (includes all fields, requires authorization)
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Get all customers for list view
     * Security: Returns CustomerListResponse without creditLimit, outstandingBalance
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerListResponse>>> getAllCustomers() {
        List<CustomerListResponse> customers = customerService.getAllCustomersForList();
        return ResponseEntity.ok(
                ApiResponse.success("Customers retrieved successfully", customers)
        );
    }

    /**
     * Get customer detail by ID
     * Security: Returns CustomerDetailResponse with all fields including creditLimit
     * Access: Restricted to SALES and ADMIN roles
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SALES') or hasRole('ADMIN') or hasRole('FINANCE')")
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> getCustomerById(@PathVariable Long id) {
        CustomerDetailResponse customer = customerService.getCustomerDetailById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Customer retrieved successfully", customer)
        );
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomerByCode(@PathVariable String code) {
        CustomerDTO customer = customerService.getCustomerByCode(code);
        return ResponseEntity.ok(
                ApiResponse.success("Customer retrieved successfully", customer)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getActiveCustomers() {
        List<CustomerDTO> customers = customerService.getActiveCustomers();
        return ResponseEntity.ok(
                ApiResponse.success("Active customers retrieved successfully", customers)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> searchCustomers(
            @RequestParam(required = false) String keyword) {

        List<CustomerDTO> customers = customerService.searchCustomers(keyword);
        return ResponseEntity.ok(
                ApiResponse.success("Search completed successfully", customers)
        );
    }

    @GetMapping("/exceeding-credit")
    @PreAuthorize("hasRole('SALES') or hasRole('ADMIN') or hasRole('FINANCE')")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getCustomersExceedingCreditLimit() {
        List<CustomerDTO> customers = customerService.getCustomersExceedingCreditLimit();
        return ResponseEntity.ok(
                ApiResponse.success("Customers exceeding credit limit retrieved successfully", customers)
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('SALES_STAFF') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerDTO>> createCustomer(
            @Valid @RequestBody CustomerDTO customerDTO) {

        CustomerDTO created = customerService.createCustomer(customerDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SALES') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerDTO>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDTO customerDTO) {

        CustomerDTO updated = customerService.updateCustomer(id, customerDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Customer updated successfully", updated)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(
                ApiResponse.success("Customer deleted successfully", null)
        );
    }
}