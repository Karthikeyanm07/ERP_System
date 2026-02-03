package com.erp.enterprise.service.sales;

import com.erp.enterprise.dto.sales.InvoiceCreateRequest;
import com.erp.enterprise.dto.sales.InvoiceDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Invoice Service Interface
 *
 * Explanation:
 * - Manages customer invoices
 * - Tracks payment status
 * - Updates customer outstanding balance
 */
public interface InvoiceService {

    InvoiceDTO createInvoice(InvoiceCreateRequest request);
    InvoiceDTO createInvoiceFromSalesOrder(Long salesOrderId, InvoiceCreateRequest request);
    InvoiceDTO getInvoiceById(Long id);
    InvoiceDTO getInvoiceByNumber(String invoiceNumber);
    List<InvoiceDTO> getAllInvoices();
    List<InvoiceDTO> getInvoicesByCustomer(Long customerId);
    List<InvoiceDTO> getInvoicesByStatus(String status);
    List<InvoiceDTO> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate);
    List<InvoiceDTO> getOverdueInvoices();

    InvoiceDTO updateInvoice(Long id, InvoiceDTO invoiceDTO);
    void deleteInvoice(Long id);

    // Update invoice after payment (internal use)
    void updateInvoiceAfterPayment(Long invoiceId);
}