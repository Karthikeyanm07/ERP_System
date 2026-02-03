package com.erp.enterprise.service.finance;

import com.erp.enterprise.dto.finance.TransactionCreateRequest;
import com.erp.enterprise.dto.finance.TransactionDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Transaction Service Interface
 */
public interface TransactionService {

    // Create transaction
    TransactionDTO createTransaction(TransactionCreateRequest request);

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