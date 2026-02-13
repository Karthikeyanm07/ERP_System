package com.erp.enterprise.repository.sales;

import com.erp.enterprise.entity.sales.Invoice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Invoice Repository
 *
 * Explanation:
 * - Manages customer invoices
 * - Tracks payment status
 * - Queries for accounts receivable reporting
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Check if invoice number exists
    boolean existsByInvoiceNumber(String invoiceNumber);

    // Find by invoice number
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    // Find invoices by customer
    List<Invoice> findByCustomerIdOrderByInvoiceDateDesc(Long customerId);

    // Find invoices by sales order
    Optional<Invoice> findBySalesOrderId(Long salesOrderId);

    // Optimized listing methods with EntityGraph to prevent N+1 queries
    @Override
    @EntityGraph(attributePaths = {"customer", "salesOrder"})
    @org.springframework.lang.NonNull
    List<Invoice> findAll();

    @EntityGraph(attributePaths = {"customer", "salesOrder"})
    List<Invoice> findByStatusOrderByInvoiceDateDesc(String status);

    @EntityGraph(attributePaths = {"customer", "salesOrder"})
    List<Invoice> findByInvoiceDateBetweenOrderByInvoiceDateDesc(LocalDate startDate, LocalDate endDate);

    @EntityGraph(attributePaths = {"customer", "salesOrder"})
    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :currentDate AND i.status != 'PAID'")
    List<Invoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);

    // Calculate total outstanding amount for customer
    // Business Logic: Sum of remaining amounts on unpaid/partial invoices
    @Query("SELECT COALESCE(SUM(i.totalAmount - i.paidAmount), 0) FROM Invoice i " +
            "WHERE i.customer.id = :customerId AND i.status IN ('UNPAID', 'PARTIAL', 'OVERDUE')")
    BigDecimal calculateOutstandingAmountForCustomer(@Param("customerId") Long customerId);
}