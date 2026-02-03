package com.erp.enterprise.repository.sales;

import com.erp.enterprise.entity.sales.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Payment Repository
 *
 * Explanation:
 * - Manages payment receipts from customers
 * - Links payments to invoices
 * - Cash flow tracking and reporting
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Check if payment number exists
    boolean existsByPaymentNumber(String paymentNumber);

    // Find by payment number
    Optional<Payment> findByPaymentNumber(String paymentNumber);

    // Find payments by invoice
    List<Payment> findByInvoiceIdOrderByPaymentDateDesc(Long invoiceId);

    // Find payments in date range
    List<Payment> findByPaymentDateBetweenOrderByPaymentDateDesc(
            LocalDate startDate, LocalDate endDate);

    // Find payments by method
    List<Payment> findByPaymentMethodOrderByPaymentDateDesc(String paymentMethod);

    // Calculate total payments for invoice
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.invoice.id = :invoiceId")
    BigDecimal calculateTotalPaymentsForInvoice(@Param("invoiceId") Long invoiceId);
}