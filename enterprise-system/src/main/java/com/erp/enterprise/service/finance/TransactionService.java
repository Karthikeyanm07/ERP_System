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
    TransactionDTO createTransaction(@org.springframework.lang.NonNull TransactionCreateRequest request);

    // Auto-create finance transaction when payment is received (Phase 2)
    void autoCreatePaymentTransaction(@org.springframework.lang.NonNull Payment payment, @org.springframework.lang.NonNull Invoice invoice);

    // Auto-create finance transaction when purchase order is received (Phase 3)
    void autoCreatePurchaseTransaction(@org.springframework.lang.NonNull PurchaseOrder purchaseOrder);

    // Get transaction by ID
    TransactionDTO getTransactionById(@org.springframework.lang.NonNull Long id);

    // Get transaction by code
    TransactionDTO getTransactionByCode(@org.springframework.lang.NonNull String transactionCode);

    // Get all transactions
    List<TransactionDTO> getAllTransactions();

    // Get transactions by date
    List<TransactionDTO> getTransactionsByDate(@org.springframework.lang.NonNull LocalDate date);

    // Get transactions in date range
    List<TransactionDTO> getTransactionsByDateRange(@org.springframework.lang.NonNull LocalDate startDate, @org.springframework.lang.NonNull LocalDate endDate);

    // Get transactions by user
    List<TransactionDTO> getTransactionsByUser(@org.springframework.lang.NonNull Long userId);

    // Get recent transactions
    @org.springframework.lang.NonNull
    List<TransactionDTO> getRecentTransactions();

    // Delete transaction
    void deleteTransaction(@org.springframework.lang.NonNull Long id);
}