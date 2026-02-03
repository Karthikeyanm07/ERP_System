package com.erp.enterprise.service.finance;

import com.erp.enterprise.dto.finance.AccountDTO;
import java.util.List;

/**
 * Account Service Interface
 */
public interface AccountService {

    // Create account
    AccountDTO createAccount(AccountDTO accountDTO);

    // Get account by ID
    AccountDTO getAccountById(Long id);

    // Get account by code
    AccountDTO getAccountByCode(String accountCode);

    // Get all accounts
    List<AccountDTO> getAllAccounts();

    // Get accounts by type
    List<AccountDTO> getAccountsByType(String accountType);

    // Get active accounts
    List<AccountDTO> getActiveAccounts();

    // Get top-level accounts (no parent)
    List<AccountDTO> getTopLevelAccounts();

    // Get child accounts
    List<AccountDTO> getChildAccounts(Long parentAccountId);

    // Search accounts
    List<AccountDTO> searchAccounts(String keyword);

    // Update account
    AccountDTO updateAccount(Long id, AccountDTO accountDTO);

    // Delete account
    void deleteAccount(Long id);

    // Update account balance (internal use)
    void updateAccountBalance(Long accountId);
}