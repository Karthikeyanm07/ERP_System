package com.erp.enterprise.service.sales.impl;

import com.erp.enterprise.dto.sales.InvoiceCreateRequest;
import com.erp.enterprise.dto.sales.InvoiceDTO;
import com.erp.enterprise.entity.sales.*;
import com.erp.enterprise.exception.BusinessException;
import com.erp.enterprise.exception.DuplicateResourceException;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.sales.*;
import com.erp.enterprise.service.sales.CustomerService;
import com.erp.enterprise.service.sales.InvoiceService;
import com.erp.enterprise.service.common.SequenceGeneratorService;
import com.erp.enterprise.util.DtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Invoice Service Implementation
 *
 * Business Logic:
 * - Creates invoices for sales
 * - Tracks payment status (UNPAID, PARTIAL, PAID, OVERDUE)
 * - Updates customer outstanding balance
 * - Links to sales orders
 * - Auto-generates invoice when sales order is delivered
 */
@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceServiceImpl.class);

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerService customerService;
    private final SequenceGeneratorService sequenceGenerator;

    @Autowired
    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              CustomerRepository customerRepository,
                              SalesOrderRepository salesOrderRepository,
                              PaymentRepository paymentRepository,
                              CustomerService customerService,
                              SequenceGeneratorService sequenceGenerator) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.paymentRepository = paymentRepository;
        this.customerService = customerService;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public InvoiceDTO createInvoice(InvoiceCreateRequest request) {
        /**
         * Business Logic:
         * - Creates standalone invoice (not linked to sales order)
         * - Validates customer
         * - Calculates total amount
         * - Updates customer outstanding balance
         */

        // Check duplicate invoice number
        if (invoiceRepository.existsByInvoiceNumber(request.getInvoiceNumber())) {
            throw new DuplicateResourceException(
                    "Invoice", "invoiceNumber", request.getInvoiceNumber());
        }

        // Validate customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer", "id", request.getCustomerId()));

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(request.getInvoiceNumber());
        invoice.setCustomer(customer);
        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setSubtotal(request.getSubtotal());
        invoice.setTaxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO);
        invoice.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);

        // Calculate total
        BigDecimal totalAmount = request.getSubtotal()
                .add(invoice.getTaxAmount())
                .subtract(invoice.getDiscountAmount());
        invoice.setTotalAmount(totalAmount);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setStatus("UNPAID");

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Update customer outstanding balance
        customerService.updateOutstandingBalance(customer.getId());

        return DtoMapper.toInvoiceDTO(savedInvoice);
    }

    @Override
    public InvoiceDTO createInvoiceFromSalesOrder(Long salesOrderId, InvoiceCreateRequest request) {
        /**
         * Business Logic:
         * - Creates invoice linked to a sales order
         * - Copies amounts from sales order
         * - One invoice per sales order
         */

        // Validate sales order
        SalesOrder salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", salesOrderId));

        // Check if invoice already exists for this sales order
        if (invoiceRepository.findBySalesOrderId(salesOrderId).isPresent()) {
            throw new BusinessException(
                    "Invoice already exists for this sales order",
                    "INVOICE_EXISTS");
        }

        // Check duplicate invoice number
        if (invoiceRepository.existsByInvoiceNumber(request.getInvoiceNumber())) {
            throw new DuplicateResourceException(
                    "Invoice", "invoiceNumber", request.getInvoiceNumber());
        }

        // Create invoice from sales order
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(request.getInvoiceNumber());
        invoice.setSalesOrder(salesOrder);
        invoice.setCustomer(salesOrder.getCustomer());
        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setSubtotal(salesOrder.getSubtotal());
        invoice.setTaxAmount(salesOrder.getTaxAmount());
        invoice.setDiscountAmount(salesOrder.getDiscountAmount());
        invoice.setTotalAmount(salesOrder.getTotalAmount());
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setStatus("UNPAID");

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Update customer outstanding balance
        customerService.updateOutstandingBalance(salesOrder.getCustomer().getId());

        return DtoMapper.toInvoiceDTO(savedInvoice);
    }

    @Override
    public InvoiceDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        return DtoMapper.toInvoiceDTO(invoice);
    }

    @Override
    public InvoiceDTO getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice", "invoiceNumber", invoiceNumber));

        return DtoMapper.toInvoiceDTO(invoice);
    }

    @Override
    public List<InvoiceDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(DtoMapper::toInvoiceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceDTO> getInvoicesByCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }

        return invoiceRepository.findByCustomerIdOrderByInvoiceDateDesc(customerId).stream()
                .map(DtoMapper::toInvoiceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceDTO> getInvoicesByStatus(String status) {
        if (!isValidInvoiceStatus(status)) {
            throw new BusinessException("Invalid invoice status: " + status, "INVALID_STATUS");
        }

        return invoiceRepository.findByStatusOrderByInvoiceDateDesc(status).stream()
                .map(DtoMapper::toInvoiceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceDTO> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date", "INVALID_DATE_RANGE");
        }

        return invoiceRepository.findByInvoiceDateBetweenOrderByInvoiceDateDesc(startDate, endDate).stream()
                .map(DtoMapper::toInvoiceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceDTO> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now()).stream()
                .map(DtoMapper::toInvoiceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceDTO updateInvoice(Long id, InvoiceDTO invoiceDTO) {
        Invoice existingInvoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        // Business Logic: Cannot update paid invoices
        if ("PAID".equals(existingInvoice.getStatus())) {
            throw new BusinessException("Cannot update paid invoices", "CANNOT_UPDATE_PAID");
        }

        existingInvoice.setInvoiceDate(invoiceDTO.getInvoiceDate());
        existingInvoice.setDueDate(invoiceDTO.getDueDate());
        existingInvoice.setSubtotal(invoiceDTO.getSubtotal());
        existingInvoice.setTaxAmount(invoiceDTO.getTaxAmount());
        existingInvoice.setDiscountAmount(invoiceDTO.getDiscountAmount());

        // Recalculate total
        BigDecimal totalAmount = invoiceDTO.getSubtotal()
                .add(invoiceDTO.getTaxAmount())
                .subtract(invoiceDTO.getDiscountAmount());
        existingInvoice.setTotalAmount(totalAmount);

        // Update status based on payment
        existingInvoice.updateStatus();

        Invoice updatedInvoice = invoiceRepository.save(existingInvoice);

        // Update customer balance
        customerService.updateOutstandingBalance(existingInvoice.getCustomer().getId());

        return DtoMapper.toInvoiceDTO(updatedInvoice);
    }

    @Override
    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        // Business Logic: Cannot delete paid or partial invoices
        if ("PAID".equals(invoice.getStatus()) || "PARTIAL".equals(invoice.getStatus())) {
            throw new BusinessException(
                    "Cannot delete paid or partially paid invoices",
                    "CANNOT_DELETE");
        }

        Long customerId = invoice.getCustomer().getId();
        invoiceRepository.delete(invoice);

        // Update customer balance
        customerService.updateOutstandingBalance(customerId);
    }

    @Override
    public void updateInvoiceAfterPayment(Long invoiceId) {
        /**
         * Business Logic:
         * - Called after payment is received
         * - Recalculates paid amount from all payments
         * - Updates invoice status
         * - Updates customer outstanding balance
         */

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));

        // Calculate total payments
        BigDecimal totalPayments = paymentRepository.calculateTotalPaymentsForInvoice(invoiceId);
        invoice.setPaidAmount(totalPayments);

        // Update status
        invoice.updateStatus();

        invoiceRepository.save(invoice);

        // Update customer balance
        customerService.updateOutstandingBalance(invoice.getCustomer().getId());
    }

    @Override
    public InvoiceDTO autoCreateInvoiceForDeliveredOrder(SalesOrder salesOrder) {
        /**
         * Auto-Invoice Generation (Phase 1)
         *
         * Called automatically when a Sales Order is DELIVERED.
         * - Generates sequential invoice number (INV-YYYY-NNN)
         * - Copies all amounts from sales order
         * - Sets due date to 30 days from today
         * - Skips if invoice already exists for this SO
         */

        // Skip if invoice already exists for this sales order
        if (invoiceRepository.findBySalesOrderId(salesOrder.getId()).isPresent()) {
            logger.info("Invoice already exists for Sales Order {}, skipping auto-creation",
                    salesOrder.getOrderNumber());
            return null;
        }

        // Generate auto-sequential invoice number
        String invoiceNumber = sequenceGenerator.nextInvoiceNumber();

        // Create invoice from sales order
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setSalesOrder(salesOrder);
        invoice.setCustomer(salesOrder.getCustomer());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setSubtotal(salesOrder.getSubtotal());
        invoice.setTaxAmount(salesOrder.getTaxAmount());
        invoice.setDiscountAmount(salesOrder.getDiscountAmount());
        invoice.setTotalAmount(salesOrder.getTotalAmount());
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setStatus("UNPAID");

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Update customer outstanding balance
        customerService.updateOutstandingBalance(salesOrder.getCustomer().getId());

        logger.info("Auto-generated Invoice {} for Sales Order {} - Amount: {}",
                invoiceNumber, salesOrder.getOrderNumber(), salesOrder.getTotalAmount());

        return DtoMapper.toInvoiceDTO(savedInvoice);
    }

    private boolean isValidInvoiceStatus(String status) {
        return status != null &&
                (status.equals("UNPAID") ||
                        status.equals("PARTIAL") ||
                        status.equals("PAID") ||
                        status.equals("OVERDUE"));
    }
}