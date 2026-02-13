package com.erp.enterprise.service.finance.impl;

import com.erp.enterprise.dto.finance.AccountDTO;
import com.erp.enterprise.entity.finance.Account;
import com.erp.enterprise.exception.BusinessException;
import com.erp.enterprise.exception.DuplicateResourceException;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.finanace.AccountRepository;
import com.erp.enterprise.repository.finanace.TransactionEntryRepository;
import com.erp.enterprise.service.finance.AccountService;
import com.erp.enterprise.service.common.AuditLogService;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Account Service Implementation
 *
 * Business Logic:
 * - Manages chart of accounts
 * - Validates account types
 * - Maintains hierarchical structure
 * - Updates account balances based on transactions
 */
@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionEntryRepository transactionEntryRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository,
                              TransactionEntryRepository transactionEntryRepository,
                              AuditLogService auditLogService) {
        this.accountRepository = accountRepository;
        this.transactionEntryRepository = transactionEntryRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public AccountDTO createAccount(@org.springframework.lang.NonNull AccountDTO accountDTO) {
        // Business Logic: Check if account code already exists
        if (accountRepository.existsByAccountCode(accountDTO.getAccountCode())) {
            throw new DuplicateResourceException(
                    "Account", "accountCode", accountDTO.getAccountCode());
        }

        // Business Logic: Validate account type
        if (!isValidAccountType(accountDTO.getAccountType())) {
            throw new BusinessException(
                    "Invalid account type: " + accountDTO.getAccountType(),
                    "INVALID_ACCOUNT_TYPE");
        }

        // Create account
        Account account = DtoMapper.toAccountEntity(accountDTO);

        // Set parent account if provided
        Long parentId = accountDTO.getParentAccountId();
        if (parentId != null) {
            Account parentAccount = accountRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Account", "id", parentId));
            account.setParentAccount(parentAccount);
        }

        // Set balance from DTO, or default to zero if not provided
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }

        Account savedAccount = accountRepository.save(account);

        // Log action
        auditLogService.log("CREATE", "ACCOUNT", savedAccount.getId(), null, 
                String.format("Created account: %s (%s)", savedAccount.getAccountName(), savedAccount.getAccountCode()));

        return DtoMapper.toAccountDTO(savedAccount);
    }

    @Override
    public AccountDTO getAccountById(@org.springframework.lang.NonNull Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        return DtoMapper.toAccountDTO(account);
    }

    @Override
    public AccountDTO getAccountByCode(@org.springframework.lang.NonNull String accountCode) {
        Account account = accountRepository.findByAccountCode(accountCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account", "accountCode", accountCode));

        return DtoMapper.toAccountDTO(account);
    }

    @Override
    public List<AccountDTO> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();

        return accounts.stream()
                .map(DtoMapper::toAccountDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountDTO> getAccountsByType(@org.springframework.lang.NonNull String accountType) {
        // Validate account type
        if (!isValidAccountType(accountType)) {
            throw new BusinessException(
                    "Invalid account type: " + accountType,
                    "INVALID_ACCOUNT_TYPE");
        }

        List<Account> accounts = accountRepository.findByAccountType(accountType);

        return accounts.stream()
                .map(DtoMapper::toAccountDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountDTO> getActiveAccounts() {
        List<Account> accounts = accountRepository.findByIsActive(true);

        return accounts.stream()
                .map(DtoMapper::toAccountDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountDTO> getTopLevelAccounts() {
        List<Account> accounts = accountRepository.findByParentAccountIsNull();

        return accounts.stream()
                .map(DtoMapper::toAccountDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountDTO> getChildAccounts(@org.springframework.lang.NonNull Long parentAccountId) {
        // Validate parent account exists
        if (!accountRepository.existsById(parentAccountId)) {
            throw new ResourceNotFoundException("Account", "id", parentAccountId);
        }

        List<Account> accounts = accountRepository.findByParentAccountId(parentAccountId);

        return accounts.stream()
                .map(DtoMapper::toAccountDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountDTO> searchAccounts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllAccounts();
        }

        List<Account> accounts = accountRepository.searchAccounts(keyword.trim());

        return accounts.stream()
                .map(DtoMapper::toAccountDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDTO updateAccount(@org.springframework.lang.NonNull Long id, @org.springframework.lang.NonNull AccountDTO accountDTO) {
        // Find existing account
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        // Business Logic: Check if new code conflicts
        if (!existingAccount.getAccountCode().equals(accountDTO.getAccountCode()) &&
                accountRepository.existsByAccountCode(accountDTO.getAccountCode())) {
            throw new DuplicateResourceException(
                    "Account", "accountCode", accountDTO.getAccountCode());
        }

        // Business Logic: Validate account type
        if (!isValidAccountType(accountDTO.getAccountType())) {
            throw new BusinessException(
                    "Invalid account type: " + accountDTO.getAccountType(),
                    "INVALID_ACCOUNT_TYPE");
        }

        // Update fields
        existingAccount.setAccountCode(accountDTO.getAccountCode());
        existingAccount.setAccountName(accountDTO.getAccountName());
        existingAccount.setAccountType(accountDTO.getAccountType());
        existingAccount.setIsActive(accountDTO.getIsActive());

        // Update parent account if provided
        Long parentId = accountDTO.getParentAccountId();
        if (parentId != null) {
            Account parentAccount = accountRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Account", "id", parentId));

            // Business Logic: Prevent circular reference
            if (parentId.equals(id)) {
                throw new BusinessException(
                        "Account cannot be its own parent",
                        "CIRCULAR_PARENT_REFERENCE");
            }

            existingAccount.setParentAccount(parentAccount);
        } else {
            existingAccount.setParentAccount(null);
        }

        Account updatedAccount = accountRepository.save(existingAccount);

        // Log action
        auditLogService.log("UPDATE", "ACCOUNT", updatedAccount.getId(), null, 
                String.format("Updated account: %s", updatedAccount.getAccountCode()));

        return DtoMapper.toAccountDTO(updatedAccount);
    }

    @Override
    public void deleteAccount(@org.springframework.lang.NonNull Long id) {
        // Check if account exists
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));

        // Business Logic: Check if account has transactions
        if (transactionEntryRepository.existsByAccountId(id)) {
            throw new BusinessException(
                    "Cannot delete account with existing transactions. Deactivate it instead to preserve financial records.",
                    "ACCOUNT_HAS_TRANSACTIONS");
        }

        // Business Logic: Check if account has child accounts
        List<Account> childAccounts = accountRepository.findByParentAccountId(id);
        if (!childAccounts.isEmpty()) {
            throw new BusinessException(
                    "Cannot delete account with child accounts. Delete or reassign child accounts first.",
                    "ACCOUNT_HAS_CHILDREN");
        }

        accountRepository.delete(account);

        // Log action
        auditLogService.log("DELETE", "ACCOUNT", id, 
                String.format("Deleted account: %s", account.getAccountCode()), null);
    }

    @Override
    public void updateAccountBalance(@org.springframework.lang.NonNull Long accountId) {
        // Find account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        // Calculate balance from transaction entries
        BigDecimal balance = transactionEntryRepository.calculateAccountBalance(accountId);

        account.setBalance(balance);
        accountRepository.save(account);

        // Log balance update (optional but good for tracking finance syncs)
        auditLogService.log("UPDATE_BALANCE", "ACCOUNT", accountId, null, 
                String.format("Balance updated to: %s", balance));
    }

    // Helper method to validate account type
    private boolean isValidAccountType(String accountType) {
        return accountType != null &&
                (accountType.equals("ASSET") ||
                        accountType.equals("LIABILITY") ||
                        accountType.equals("EQUITY") ||
                        accountType.equals("REVENUE") ||
                        accountType.equals("EXPENSE"));
    }
}