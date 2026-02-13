package com.erp.enterprise.service.finance.impl;

import com.erp.enterprise.dto.finance.TransactionCreateRequest;
import com.erp.enterprise.dto.finance.TransactionDTO;
import com.erp.enterprise.dto.finance.TransactionEntryDTO;
import com.erp.enterprise.entity.finance.Account;
import com.erp.enterprise.entity.finance.Transaction;
import com.erp.enterprise.entity.finance.TransactionEntry;
import com.erp.enterprise.entity.hr.User;
import com.erp.enterprise.entity.sales.Invoice;
import com.erp.enterprise.entity.sales.Payment;
import com.erp.enterprise.entity.inventory.PurchaseOrder;
import com.erp.enterprise.exception.BusinessException;
import com.erp.enterprise.exception.DuplicateResourceException;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.finanace.AccountRepository;
import com.erp.enterprise.repository.finanace.TransactionRepository;
import com.erp.enterprise.repository.hr.UserRepository;
import com.erp.enterprise.service.finance.AccountService;
import com.erp.enterprise.service.finance.TransactionService;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Transaction Service Implementation
 *
 * Business Logic:
 * - Implements double-entry bookkeeping
 * - Validates that debits equal credits
 * - Updates account balances after each transaction
 * - Auto-creates transactions for payments and purchase orders
 * - Ensures data integrity
 */
@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final SequenceGeneratorService sequenceGenerator;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  AccountRepository accountRepository,
                                  UserRepository userRepository,
                                  AccountService accountService,
                                  SequenceGeneratorService sequenceGenerator) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public TransactionDTO createTransaction(TransactionCreateRequest request) {
        // Business Logic: Check if transaction code exists
        if (transactionRepository.existsByTransactionCode(request.getTransactionCode())) {
            throw new DuplicateResourceException(
                    "Transaction", "transactionCode", request.getTransactionCode());
        }

        // Validate user
        User user = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", request.getCreatedById()));

        // Business Logic: Validate entries (must have at least 2)
        if (request.getEntries() == null || request.getEntries().size() < 2) {
            throw new BusinessException(
                    "Transaction must have at least 2 entries (debit and credit)",
                    "INSUFFICIENT_ENTRIES");
        }

        // Business Logic: Calculate total debits and credits
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (TransactionEntryDTO entryDTO : request.getEntries()) {
            if (entryDTO.getEntryType().equals("DEBIT")) {
                totalDebits = totalDebits.add(entryDTO.getAmount());
            } else if (entryDTO.getEntryType().equals("CREDIT")) {
                totalCredits = totalCredits.add(entryDTO.getAmount());
            } else {
                throw new BusinessException(
                        "Invalid entry type: " + entryDTO.getEntryType(),
                        "INVALID_ENTRY_TYPE");
            }
        }

        // Business Logic: Validate double-entry (Debits must equal Credits)
        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new BusinessException(
                    String.format("Debits (%.2f) must equal Credits (%.2f)",
                            totalDebits, totalCredits),
                    "UNBALANCED_TRANSACTION");
        }

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setTransactionCode(request.getTransactionCode());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription());
        transaction.setReferenceNumber(request.getReferenceNumber());
        transaction.setCreatedBy(user);

        // Create transaction entries
        for (TransactionEntryDTO entryDTO : request.getEntries()) {
            // Validate account exists
            Account account = accountRepository.findById(entryDTO.getAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Account", "id", entryDTO.getAccountId()));

            // Create entry
            TransactionEntry entry = new TransactionEntry();
            entry.setAccount(account);
            entry.setEntryType(entryDTO.getEntryType());
            entry.setAmount(entryDTO.getAmount());
            entry.setDescription(entryDTO.getDescription());

            // Add to transaction
            transaction.addEntry(entry);
        }

        // Save transaction
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Update account balances
        for (TransactionEntry entry : savedTransaction.getEntries()) {
            accountService.updateAccountBalance(entry.getAccount().getId());
        }

        return DtoMapper.toTransactionDTO(savedTransaction);
    }

    @Override
    public TransactionDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        return DtoMapper.toTransactionDTO(transaction);
    }

    @Override
    public TransactionDTO getTransactionByCode(String transactionCode) {
        Transaction transaction = transactionRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction", "transactionCode", transactionCode));

        return DtoMapper.toTransactionDTO(transaction);
    }

    @Override
    public List<TransactionDTO> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();

        return transactions.stream()
                .map(DtoMapper::toTransactionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getTransactionsByDate(LocalDate date) {
        List<Transaction> transactions =
                transactionRepository.findByTransactionDateOrderByCreatedAtDesc(date);

        return transactions.stream()
                .map(DtoMapper::toTransactionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(
                    "Start date cannot be after end date",
                    "INVALID_DATE_RANGE");
        }

        List<Transaction> transactions =
                transactionRepository.findByTransactionDateBetweenOrderByTransactionDateDesc(
                        startDate, endDate);

        return transactions.stream()
                .map(DtoMapper::toTransactionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getTransactionsByUser(Long userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        List<Transaction> transactions =
                transactionRepository.findByCreatedByIdOrderByCreatedAtDesc(userId);

        return transactions.stream()
                .map(DtoMapper::toTransactionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getRecentTransactions() {
        List<Transaction> transactions = transactionRepository.findRecentTransactions();

        return transactions.stream()
                .limit(20)  // Return last 20 transactions
                .map(DtoMapper::toTransactionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTransaction(Long id) {
        // Find transaction
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        // Get affected accounts before deleting
        List<Long> affectedAccountIds = transaction.getEntries().stream()
                .map(entry -> entry.getAccount().getId())
                .distinct()
                .collect(Collectors.toList());

        // Delete transaction (cascade will delete entries)
        transactionRepository.delete(transaction);

        // Update balances of affected accounts
        for (Long accountId : affectedAccountIds) {
            accountService.updateAccountBalance(accountId);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // AUTO-TRANSACTION METHODS (Cross-module integration)
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public void autoCreatePaymentTransaction(Payment payment, Invoice invoice) {
        /**
         * Phase 2: Payment → Finance Auto-Transaction
         *
         * When a payment is received:
         * - Debit: Cash/Bank Account (ASSET increases)
         * - Credit: Accounts Receivable (ASSET decreases — customer pays off debt)
         *
         * Uses first available ASSET account as cash/bank.
         * If no accounts exist, logs a warning and skips.
         */
        try {
            // Find a Cash/Bank account (ASSET type)
            Optional<Account> cashAccount = accountRepository.findByAccountType("ASSET")
                    .stream().findFirst();

            if (cashAccount.isEmpty()) {
                logger.warn("No ASSET account found for auto-transaction. " +
                        "Please create accounts in Finance module. Skipping auto-transaction for Payment {}",
                        payment.getPaymentNumber());
                return;
            }

            // Find or use same account for Accounts Receivable
            // In a real ERP, AR would be a separate account. Here we use the second ASSET or same.
            List<Account> assetAccounts = accountRepository.findByAccountType("ASSET");
            Account debitAccount = assetAccounts.get(0); // Cash/Bank
            Account creditAccount = assetAccounts.size() > 1 ? assetAccounts.get(1) : assetAccounts.get(0);

            // Also check for REVENUE type accounts
            List<Account> revenueAccounts = accountRepository.findByAccountType("REVENUE");
            if (!revenueAccounts.isEmpty()) {
                creditAccount = revenueAccounts.get(0); // Sales Revenue
            }

            String txnCode = sequenceGenerator.nextTransactionCode();

            // Create transaction
            Transaction transaction = new Transaction();
            transaction.setTransactionCode(txnCode);
            transaction.setTransactionDate(payment.getPaymentDate());
            transaction.setDescription(String.format(
                    "Payment %s received for Invoice %s - %s",
                    payment.getPaymentNumber(),
                    invoice.getInvoiceNumber(),
                    payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "N/A"));
            transaction.setReferenceNumber(payment.getPaymentNumber());

            // Debit entry: Cash/Bank account increases
            TransactionEntry debitEntry = new TransactionEntry();
            debitEntry.setAccount(debitAccount);
            debitEntry.setEntryType("DEBIT");
            debitEntry.setAmount(payment.getAmount());
            debitEntry.setDescription("Payment received - " + payment.getPaymentMethod());
            transaction.addEntry(debitEntry);

            // Credit entry: Revenue/AR account
            TransactionEntry creditEntry = new TransactionEntry();
            creditEntry.setAccount(creditAccount);
            creditEntry.setEntryType("CREDIT");
            creditEntry.setAmount(payment.getAmount());
            creditEntry.setDescription("Invoice " + invoice.getInvoiceNumber() + " payment");
            transaction.addEntry(creditEntry);

            // Save transaction
            Transaction saved = transactionRepository.save(transaction);

            // Update account balances
            accountService.updateAccountBalance(debitAccount.getId());
            accountService.updateAccountBalance(creditAccount.getId());

            logger.info("Auto-created finance transaction {} for Payment {} - Amount: {}",
                    txnCode, payment.getPaymentNumber(), payment.getAmount());

        } catch (Exception e) {
            // Don't fail the payment if transaction creation fails
            logger.error("Failed to auto-create finance transaction for Payment {}: {}",
                    payment.getPaymentNumber(), e.getMessage());
        }
    }

    @Override
    public void autoCreatePurchaseTransaction(PurchaseOrder purchaseOrder) {
        /**
         * Phase 3: PO Received → Finance Auto-Transaction
         *
         * When a purchase order is received:
         * - Debit: Expense/Purchase Account (EXPENSE increases)
         * - Credit: Cash/Bank Account (ASSET decreases — money goes out)
         *
         * Uses first available EXPENSE and ASSET accounts.
         * If no accounts exist, logs a warning and skips.
         */
        try {
            // Find an Expense account
            Optional<Account> expenseAccount = accountRepository.findByAccountType("EXPENSE")
                    .stream().findFirst();

            // Find a Cash/Bank account (ASSET type)
            Optional<Account> cashAccount = accountRepository.findByAccountType("ASSET")
                    .stream().findFirst();

            if (expenseAccount.isEmpty() || cashAccount.isEmpty()) {
                logger.warn("No EXPENSE or ASSET account found for auto-transaction. " +
                        "Please create accounts in Finance module. Skipping auto-transaction for PO {}",
                        purchaseOrder.getPoNumber());
                return;
            }

            String txnCode = sequenceGenerator.nextTransactionCode();

            // Create transaction
            Transaction transaction = new Transaction();
            transaction.setTransactionCode(txnCode);
            transaction.setTransactionDate(LocalDate.now());
            transaction.setDescription(String.format(
                    "Purchase Order %s received from %s",
                    purchaseOrder.getPoNumber(),
                    purchaseOrder.getSupplier().getName()));
            transaction.setReferenceNumber(purchaseOrder.getPoNumber());

            // Debit entry: Expense/Purchase account increases
            TransactionEntry debitEntry = new TransactionEntry();
            debitEntry.setAccount(expenseAccount.get());
            debitEntry.setEntryType("DEBIT");
            debitEntry.setAmount(purchaseOrder.getTotalAmount());
            debitEntry.setDescription("PO " + purchaseOrder.getPoNumber() + " - purchase expense");
            transaction.addEntry(debitEntry);

            // Credit entry: Cash/Bank account decreases
            TransactionEntry creditEntry = new TransactionEntry();
            creditEntry.setAccount(cashAccount.get());
            creditEntry.setEntryType("CREDIT");
            creditEntry.setAmount(purchaseOrder.getTotalAmount());
            creditEntry.setDescription("PO " + purchaseOrder.getPoNumber() + " - payment to supplier");
            transaction.addEntry(creditEntry);

            // Save transaction
            Transaction saved = transactionRepository.save(transaction);

            // Update account balances
            accountService.updateAccountBalance(expenseAccount.get().getId());
            accountService.updateAccountBalance(cashAccount.get().getId());

            logger.info("Auto-created finance transaction {} for PO {} - Amount: {}",
                    txnCode, purchaseOrder.getPoNumber(), purchaseOrder.getTotalAmount());

        } catch (Exception e) {
            // Don't fail the PO receive if transaction creation fails
            logger.error("Failed to auto-create finance transaction for PO {}: {}",
                    purchaseOrder.getPoNumber(), e.getMessage());
        }
    }
}