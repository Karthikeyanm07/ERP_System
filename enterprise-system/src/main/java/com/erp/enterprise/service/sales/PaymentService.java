package com.erp.enterprise.service.sales;

import com.erp.enterprise.dto.sales.PaymentCreateRequest;
import com.erp.enterprise.dto.sales.PaymentDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Payment Service Interface
 *
 * Explanation:
 * - Manages payment receipts from customers
 * - Links payments to invoices
 * - Updates invoice status after payment
 * - Updates customer outstanding balance
 */
public interface PaymentService {

    PaymentDTO createPayment(PaymentCreateRequest request);
    PaymentDTO getPaymentById(Long id);
    PaymentDTO getPaymentByNumber(String paymentNumber);
    List<PaymentDTO> getAllPayments();
    List<PaymentDTO> getPaymentsByInvoice(Long invoiceId);
    List<PaymentDTO> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate);
    List<PaymentDTO> getPaymentsByMethod(String paymentMethod);

    void deletePayment(Long id);
}