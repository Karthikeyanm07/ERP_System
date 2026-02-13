package com.erp.enterprise.service.finance;

import com.erp.enterprise.dto.finance.TransactionCreateRequest;
import com.erp.enterprise.dto.finance.TransactionDTO;
import com.erp.enterprise.entity.sales.Invoice;
import com.erp.enterprise.entity.sales.Payment;
import com.erp.enterprise.entity.inventory.PurchaseOrder;

import java.time.LocalDate;
import java.util.List;

/**
 * Transaction Service Interface
 */
public interface TransactionService {

    // Create transaction
    TransactionDTO createTransaction(TransactionCreateRequest request);

    // Auto-create finance transaction when payment is received (Phase 2)
    void autoCreatePaymentTransaction(Payment payment, Invoice invoice);

    // Auto-create finance transaction when purchase order is received (Phase 3)
    void autoCreatePurchaseTransaction(PurchaseOrder purchaseOrder);

    // Get transaction by ID
    TransactionDTO getTransactionById(Long id);

    // Get transaction by code
    TransactionDTO getTransactionByCode(String transactionCode);

    // Get all transactions
    List<TransactionDTO> getAllTransactions();

    // Get transactions by date
    List<TransactionDTO> getTransactionsByDate(LocalDate date);

    // Get transactions in date range
    List<TransactionDTO> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate);

    // Get transactions by user
    List<TransactionDTO> getTransactionsByUser(Long userId);

    // Get recent transactions
    List<TransactionDTO> getRecentTransactions();

    // Delete transaction
    void deleteTransaction(Long id);
}