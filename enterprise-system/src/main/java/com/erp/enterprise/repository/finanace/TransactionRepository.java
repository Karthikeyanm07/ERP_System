package com.erp.enterprise.repository.finanace;

import com.erp.enterprise.entity.finance.Transaction;
import org.springframework.data.jpa.repository.EntityGraph;
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

    // Optimized listing methods with EntityGraph to prevent N+1 queries
    @Override
    @EntityGraph(attributePaths = {"entries", "createdBy"})
    @org.springframework.lang.NonNull
    List<Transaction> findAll();

    // Find transactions by date
    @EntityGraph(attributePaths = {"entries", "createdBy"})
    List<Transaction> findByTransactionDateOrderByCreatedAtDesc(LocalDate date);

    // Find transactions in date range
    @EntityGraph(attributePaths = {"entries", "createdBy"})
    List<Transaction> findByTransactionDateBetweenOrderByTransactionDateDesc(
            LocalDate startDate, LocalDate endDate);

    // Find transactions created by user
    List<Transaction> findByCreatedByIdOrderByCreatedAtDesc(Long userId);

    // Find transactions by reference number
    List<Transaction> findByReferenceNumber(String referenceNumber);

    // Get recent transactions
    @EntityGraph(attributePaths = {"entries", "createdBy"})
    @Query("SELECT t FROM Transaction t ORDER BY t.transactionDate DESC, t.createdAt DESC")
    List<Transaction> findRecentTransactions();
}