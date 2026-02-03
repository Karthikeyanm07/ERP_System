package com.erp.enterprise.repository.finanace;

import com.erp.enterprise.entity.finance.TransactionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Transaction Entry Repository
 */
@Repository
public interface TransactionEntryRepository extends JpaRepository<TransactionEntry, Long> {

    // Find entries by transaction
    List<TransactionEntry> findByTransactionId(Long transactionId);

    // Find entries by account
    List<TransactionEntry> findByAccountIdOrderByTransactionTransactionDateDesc(Long accountId);

    // Find entries by account and entry type
    List<TransactionEntry> findByAccountIdAndEntryType(Long accountId, String entryType);

    // Calculate total debits for account
    @Query("SELECT COALESCE(SUM(te.amount), 0) FROM TransactionEntry te " +
            "WHERE te.account.id = :accountId AND te.entryType = 'DEBIT'")
    BigDecimal sumDebitsByAccount(@Param("accountId") Long accountId);

    // Calculate total credits for account
    @Query("SELECT COALESCE(SUM(te.amount), 0) FROM TransactionEntry te " +
            "WHERE te.account.id = :accountId AND te.entryType = 'CREDIT'")
    BigDecimal sumCreditsByAccount(@Param("accountId") Long accountId);

    // Get account balance (Debits - Credits)
    @Query("SELECT COALESCE(SUM(CASE WHEN te.entryType = 'DEBIT' THEN te.amount ELSE -te.amount END), 0) " +
            "FROM TransactionEntry te WHERE te.account.id = :accountId")
    BigDecimal calculateAccountBalance(@Param("accountId") Long accountId);

    // Get entries for account in date range
    @Query("SELECT te FROM TransactionEntry te " +
            "WHERE te.account.id = :accountId " +
            "AND te.transaction.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY te.transaction.transactionDate DESC")
    List<TransactionEntry> findByAccountAndDateRange(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}