package com.erp.enterprise.repository.sales;

import com.erp.enterprise.entity.sales.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Customer Repository
 *
 * Explanation:
 * - Provides database operations for customers
 * - Custom queries for searching and filtering
 * - Credit limit management queries
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Override
    @org.springframework.lang.NonNull
    List<Customer> findAll();

    // Check if customer code exists
    boolean existsByCustomerCode(String customerCode);

    // Find customer by code
    Optional<Customer> findByCustomerCode(String customerCode);

    // Find active customers
    List<Customer> findByIsActive(Boolean isActive);

    // Search customers by name, code, or contact
    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.customerCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Customer> searchCustomers(@Param("keyword") String keyword);

    // Find customers exceeding credit limit
    // Business Logic: Used for credit management alerts
    @Query("SELECT c FROM Customer c WHERE c.outstandingBalance > c.creditLimit")
    List<Customer> findCustomersExceedingCreditLimit();
}