package com.erp.enterprise.repository.finanace;

import com.erp.enterprise.entity.finance.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Transaction Repository
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Check if transaction code exists
    boolean existsByTransactionCode(String transactionCode);

    // Find transaction by code
    Optional<Transaction> findByTransactionCode(String transactionCode);

    // Find transactions by date
    List<Transaction> findByTransactionDateOrderByCreatedAtDesc(LocalDate date);

    // Find transactions in date range
    List<Transaction> findByTransactionDateBetweenOrderByTransactionDateDesc(
            LocalDate startDate, LocalDate endDate);

    // Find transactions created by user
    List<Transaction> findByCreatedByIdOrderByCreatedAtDesc(Long userId);

    // Find transactions by reference number
    List<Transaction> findByReferenceNumber(String referenceNumber);

    // Get recent transactions
    @Query("SELECT t FROM Transaction t ORDER BY t.transactionDate DESC, t.createdAt DESC")
    List<Transaction> findRecentTransactions();
}