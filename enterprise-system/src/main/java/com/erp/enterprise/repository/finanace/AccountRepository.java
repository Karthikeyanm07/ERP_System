package com.erp.enterprise.repository.finanace;

import com.erp.enterprise.entity.finance.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Account Repository
 *
 * Business Logic: Query chart of accounts
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Override
    @EntityGraph(attributePaths = {"parentAccount"})
    @org.springframework.lang.NonNull
    List<Account> findAll();

    // Check if account code exists
    boolean existsByAccountCode(String accountCode);

    // Find account by code
    Optional<Account> findByAccountCode(String accountCode);

    // Find accounts by type
    List<Account> findByAccountType(String accountType);

    // Find active accounts
    List<Account> findByIsActive(Boolean isActive);

    // Find accounts by parent account
    List<Account> findByParentAccountId(Long parentAccountId);

    // Find top-level accounts (no parent)
    List<Account> findByParentAccountIsNull();

    // Search accounts by name or code
    @Query("SELECT a FROM Account a WHERE " +
            "LOWER(a.accountName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.accountCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Account> searchAccounts(@Param("keyword") String keyword);
}