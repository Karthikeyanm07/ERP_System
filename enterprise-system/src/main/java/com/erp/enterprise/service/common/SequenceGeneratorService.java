package com.erp.enterprise.service.common;

import com.erp.enterprise.repository.finanace.ExpenseRepository;
import com.erp.enterprise.repository.finanace.TransactionRepository;
import com.erp.enterprise.repository.inventory.PurchaseOrderRepository;
import com.erp.enterprise.repository.sales.InvoiceRepository;
import com.erp.enterprise.repository.sales.PaymentRepository;
import com.erp.enterprise.repository.sales.SalesOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Sequence Generator Service
 *
 * Generates auto-sequential document numbers for all modules:
 * - SO-2026-001  ... Sales Orders
 * - PO-2026-001  ... Purchase Orders
 * - INV-2026-001 ... Invoices
 * - PAY-2026-001 ... Payments
 * - TXN-2026-001 ... Transactions
 * - EXP-2026-001 ... Expenses
 */
@Service
public class SequenceGeneratorService {

    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final ExpenseRepository expenseRepository;

    @Autowired
    public SequenceGeneratorService(SalesOrderRepository salesOrderRepository,
                                     PurchaseOrderRepository purchaseOrderRepository,
                                     InvoiceRepository invoiceRepository,
                                     PaymentRepository paymentRepository,
                                     TransactionRepository transactionRepository,
                                     ExpenseRepository expenseRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
        this.expenseRepository = expenseRepository;
    }

    /** SO-2026-001 */
    public String nextSalesOrderNumber() {
        String prefix = "SO-" + LocalDate.now().getYear() + "-";
        long count = salesOrderRepository.count() + 1;
        return prefix + String.format("%03d", count);
    }

    /** PO-2026-001 */
    public String nextPurchaseOrderNumber() {
        String prefix = "PO-" + LocalDate.now().getYear() + "-";
        long count = purchaseOrderRepository.count() + 1;
        return prefix + String.format("%03d", count);
    }

    /** INV-2026-001 */
    public String nextInvoiceNumber() {
        String prefix = "INV-" + LocalDate.now().getYear() + "-";
        long count = invoiceRepository.count() + 1;
        return prefix + String.format("%03d", count);
    }

    /** PAY-2026-001 */
    public String nextPaymentNumber() {
        String prefix = "PAY-" + LocalDate.now().getYear() + "-";
        long count = paymentRepository.count() + 1;
        return prefix + String.format("%03d", count);
    }

    /** TXN-2026-001 */
    public String nextTransactionCode() {
        String prefix = "TXN-" + LocalDate.now().getYear() + "-";
        long count = transactionRepository.count() + 1;
        return prefix + String.format("%03d", count);
    }

    /** EXP-2026-001 */
    public String nextExpenseCode() {
        String prefix = "EXP-" + LocalDate.now().getYear() + "-";
        long count = expenseRepository.count() + 1;
        return prefix + String.format("%03d", count);
    }
}
