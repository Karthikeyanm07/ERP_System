package com.erp.enterprise.service.sales.impl;

import com.erp.enterprise.dto.sales.CustomerDTO;
import com.erp.enterprise.dto.sales.CustomerListResponse;
import com.erp.enterprise.dto.sales.CustomerDetailResponse;
import com.erp.enterprise.entity.sales.Customer;
import com.erp.enterprise.exception.DuplicateResourceException;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.sales.CustomerRepository;
import com.erp.enterprise.repository.sales.InvoiceRepository;
import com.erp.enterprise.service.sales.CustomerService;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer Service Implementation
 *
 * Business Logic:
 * - Credit limit management
 * - Outstanding balance tracking
 * - Customer lifecycle management
 * - Secure response DTOs for list/detail views
 */
@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository,
                               InvoiceRepository invoiceRepository) {
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        // Check duplicate customer code
        if (customerRepository.existsByCustomerCode(customerDTO.getCustomerCode())) {
            throw new DuplicateResourceException(
                    "Customer", "customerCode", customerDTO.getCustomerCode());
        }

        Customer customer = DtoMapper.toCustomerEntity(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);

        return DtoMapper.toCustomerDTO(savedCustomer);
    }

    @Override
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return DtoMapper.toCustomerDTO(customer);
    }

    @Override
    public CustomerDTO getCustomerByCode(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer", "customerCode", customerCode));
        return DtoMapper.toCustomerDTO(customer);
    }

    @Override
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(DtoMapper::toCustomerDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerDTO> getActiveCustomers() {
        return customerRepository.findByIsActive(true).stream()
                .map(DtoMapper::toCustomerDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCustomers();
        }

        return customerRepository.searchCustomers(keyword.trim()).stream()
                .map(DtoMapper::toCustomerDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerDTO> getCustomersExceedingCreditLimit() {
        return customerRepository.findCustomersExceedingCreditLimit().stream()
                .map(DtoMapper::toCustomerDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        if (!existingCustomer.getCustomerCode().equals(customerDTO.getCustomerCode()) &&
                customerRepository.existsByCustomerCode(customerDTO.getCustomerCode())) {
            throw new DuplicateResourceException(
                    "Customer", "customerCode", customerDTO.getCustomerCode());
        }

        existingCustomer.setCustomerCode(customerDTO.getCustomerCode());
        existingCustomer.setName(customerDTO.getName());
        existingCustomer.setContactPerson(customerDTO.getContactPerson());
        existingCustomer.setEmail(customerDTO.getEmail());
        existingCustomer.setPhone(customerDTO.getPhone());
        existingCustomer.setAddress(customerDTO.getAddress());
        existingCustomer.setCity(customerDTO.getCity());
        existingCustomer.setCountry(customerDTO.getCountry());
        existingCustomer.setCreditLimit(customerDTO.getCreditLimit());
        existingCustomer.setIsActive(customerDTO.getIsActive());

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return DtoMapper.toCustomerDTO(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        customerRepository.delete(customer);
    }

    @Override
    public void updateOutstandingBalance(Long customerId) {
        /**
         * Business Logic:
         * - Calculates total unpaid/partial invoices
         * - Updates customer's outstanding balance
         * - Used after invoice creation or payment
         */
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        BigDecimal outstandingAmount = invoiceRepository
                .calculateOutstandingAmountForCustomer(customerId);

        customer.setOutstandingBalance(outstandingAmount);
        customerRepository.save(customer);
    }

    // ==================== Secure Response DTOs ====================

    /**
     * Get all customers for list view (excludes sensitive fields)
     * 
     * Security: Returns CustomerListResponse without:
     * - creditLimit
     * - outstandingBalance
     * - address, country
     */
    @Override
    @Transactional(readOnly = true)
    public List<CustomerListResponse> getAllCustomersForList() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(DtoMapper::toCustomerListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get customer detail by ID (includes all fields)
     * 
     * Security: Returns CustomerDetailResponse with all fields
     * Access: Should be restricted to authorized users via controller
     */
    @Override
    @Transactional(readOnly = true)
    public CustomerDetailResponse getCustomerDetailById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return DtoMapper.toCustomerDetailResponse(customer);
    }
}