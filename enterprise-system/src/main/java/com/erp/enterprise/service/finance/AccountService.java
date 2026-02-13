package com.erp.enterprise.service.finance;

import com.erp.enterprise.dto.finance.AccountDTO;
import java.util.List;

/**
 * Account Service Interface
 */
public interface AccountService {

    // Create account
    AccountDTO createAccount(@org.springframework.lang.NonNull AccountDTO accountDTO);

    // Get account by ID
    AccountDTO getAccountById(@org.springframework.lang.NonNull Long id);

    // Get account by code
    AccountDTO getAccountByCode(@org.springframework.lang.NonNull String accountCode);

    // Get all accounts
    List<AccountDTO> getAllAccounts();

    // Get accounts by type
    List<AccountDTO> getAccountsByType(@org.springframework.lang.NonNull String accountType);

    // Get active accounts
    List<AccountDTO> getActiveAccounts();

    // Get top-level accounts (no parent)
    List<AccountDTO> getTopLevelAccounts();

    // Get child accounts
    List<AccountDTO> getChildAccounts(@org.springframework.lang.NonNull Long parentAccountId);

    // Search accounts
    List<AccountDTO> searchAccounts(String keyword);

    // Update account
    AccountDTO updateAccount(@org.springframework.lang.NonNull Long id, @org.springframework.lang.NonNull AccountDTO accountDTO);

    // Delete account
    void deleteAccount(@org.springframework.lang.NonNull Long id);

    // Update account balance (internal use)
    void updateAccountBalance(@org.springframework.lang.NonNull Long accountId);
}