package com.erp.enterprise.service.sales;

import com.erp.enterprise.dto.sales.CustomerDTO;
import com.erp.enterprise.dto.sales.CustomerListResponse;
import com.erp.enterprise.dto.sales.CustomerDetailResponse;
import java.util.List;

/**
 * Customer Service Interface
 *
 * Explanation:
 * - Manages customer master data
 * - Handles credit limit validation
 * - Tracks outstanding balances
 * - Provides secure response DTOs for list/detail views
 */
public interface CustomerService {

    CustomerDTO createCustomer(CustomerDTO customerDTO);
    CustomerDTO getCustomerById(Long id);
    CustomerDTO getCustomerByCode(String customerCode);
    List<CustomerDTO> getAllCustomers();
    List<CustomerDTO> getActiveCustomers();
    List<CustomerDTO> searchCustomers(String keyword);
    List<CustomerDTO> getCustomersExceedingCreditLimit();
    CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO);
    void deleteCustomer(Long id);

    // Update outstanding balance (internal use)
    void updateOutstandingBalance(Long customerId);

    // ==================== Secure Response DTOs ====================
    
    /**
     * Get all customers for list view (excludes sensitive fields)
     * Returns: CustomerListResponse without creditLimit, outstandingBalance
     */
    List<CustomerListResponse> getAllCustomersForList();

    /**
     * Get customer detail by ID (includes all fields for authorized users)
     * Returns: CustomerDetailResponse with all fields including creditLimit
     */
    CustomerDetailResponse getCustomerDetailById(Long id);
}