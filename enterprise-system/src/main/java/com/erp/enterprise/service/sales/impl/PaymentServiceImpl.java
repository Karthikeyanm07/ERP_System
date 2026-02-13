package com.erp.enterprise.service.sales.impl;

import com.erp.enterprise.dto.sales.PaymentCreateRequest;
import com.erp.enterprise.dto.sales.PaymentDTO;
import com.erp.enterprise.entity.hr.User;
import com.erp.enterprise.entity.sales.Invoice;
import com.erp.enterprise.entity.sales.Payment;
import com.erp.enterprise.exception.BusinessException;
import com.erp.enterprise.exception.DuplicateResourceException;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.hr.UserRepository;
import com.erp.enterprise.repository.sales.InvoiceRepository;
import com.erp.enterprise.repository.sales.PaymentRepository;
import com.erp.enterprise.service.sales.InvoiceService;
import com.erp.enterprise.service.sales.PaymentService;
import com.erp.enterprise.service.common.SequenceGeneratorService;
import com.erp.enterprise.service.finance.TransactionService;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Payment Service Implementation
 *
 * Business Logic:
 * - Records payment receipts from customers
 * - Links payment to invoice
 * - Validates payment amount (can't exceed remaining balance)
 * - Updates invoice status after payment
 * - Multiple payments can be made against one invoice
 * - Updates customer outstanding balance
 * - Auto-creates finance transaction on payment
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;
    private final TransactionService transactionService;
    private final SequenceGeneratorService sequenceGenerator;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              InvoiceRepository invoiceRepository,
                              UserRepository userRepository,
                              InvoiceService invoiceService,
                              TransactionService transactionService,
                              SequenceGeneratorService sequenceGenerator) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.invoiceService = invoiceService;
        this.transactionService = transactionService;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public PaymentDTO createPayment(PaymentCreateRequest request) {
        /**
         * Business Logic:
         * 1. Validate payment number uniqueness
         * 2. Validate invoice and user
         * 3. Check payment amount doesn't exceed remaining balance
         * 4. Create payment record
         * 5. Update invoice paid amount and status
         * 6. Update customer outstanding balance
         * 7. AUTO: Create finance transaction (debit cash, credit revenue)
         */

        // Auto-generate payment number if blank
        String paymentNumber = request.getPaymentNumber();
        if (paymentNumber == null || paymentNumber.isBlank()) {
            paymentNumber = sequenceGenerator.nextPaymentNumber();
        }
        if (paymentRepository.existsByPaymentNumber(paymentNumber)) {
            throw new DuplicateResourceException(
                    "Payment", "paymentNumber", paymentNumber);
        }

        // Validate invoice
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice", "id", request.getInvoiceId()));

        // Validate user — default to user 1 if not provided
        Long userId = request.getCreatedById() != null ? request.getCreatedById() : 1L;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", userId));

        // Business Logic: Validate payment amount
        BigDecimal remainingAmount = invoice.getRemainingAmount();
        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new BusinessException(
                    String.format("Payment amount (%.2f) exceeds remaining invoice amount (%.2f)",
                            request.getAmount(), remainingAmount),
                    "PAYMENT_EXCEEDS_BALANCE"
            );
        }

        // Business Logic: Cannot pay fully paid invoices
        if ("PAID".equals(invoice.getStatus())) {
            throw new BusinessException(
                    "Invoice is already fully paid",
                    "INVOICE_ALREADY_PAID"
            );
        }

        // Create payment
        Payment payment = new Payment();
        payment.setPaymentNumber(paymentNumber);
        payment.setInvoice(invoice);
        payment.setPaymentDate(request.getPaymentDate());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setReferenceNumber(request.getReferenceNumber());
        payment.setRemarks(request.getRemarks());
        payment.setCreatedBy(user);

        Payment savedPayment = paymentRepository.save(payment);

        // Update invoice after payment
        invoiceService.updateInvoiceAfterPayment(invoice.getId());

        // Phase 2: Auto-create finance transaction
        transactionService.autoCreatePaymentTransaction(savedPayment, invoice);

        return DtoMapper.toPaymentDTO(savedPayment);
    }

    @Override
    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        return DtoMapper.toPaymentDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentByNumber(String paymentNumber) {
        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment", "paymentNumber", paymentNumber));

        return DtoMapper.toPaymentDTO(payment);
    }

    @Override
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(DtoMapper::toPaymentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByInvoice(Long invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new ResourceNotFoundException("Invoice", "id", invoiceId);
        }

        return paymentRepository.findByInvoiceIdOrderByPaymentDateDesc(invoiceId).stream()
                .map(DtoMapper::toPaymentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date", "INVALID_DATE_RANGE");
        }

        return paymentRepository.findByPaymentDateBetweenOrderByPaymentDateDesc(startDate, endDate).stream()
                .map(DtoMapper::toPaymentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByMethod(String paymentMethod) {
        return paymentRepository.findByPaymentMethodOrderByPaymentDateDesc(paymentMethod).stream()
                .map(DtoMapper::toPaymentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePayment(Long id) {
        /**
         * Business Logic:
         * - Deletes payment record
         * - Recalculates invoice paid amount
         * - Updates invoice status
         * - Updates customer outstanding balance
         */

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        Long invoiceId = payment.getInvoice().getId();

        paymentRepository.delete(payment);

        // Update invoice after payment deletion
        invoiceService.updateInvoiceAfterPayment(invoiceId);
    }
}