package com.accounting.repository;

import com.accounting.model.Account;
import com.accounting.model.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByCode(String code);
    boolean existsByCode(String code);

    List<Account> findByAccountType(AccountType accountType);
    List<Account> findByAccountTypeIn(List<AccountType> accountTypes);

    @Query("SELECT a FROM Account a WHERE a.isActive = true ORDER BY a.code")
    List<Account> findAllActive();

    @Query("SELECT a FROM Account a WHERE a.isActive = true AND a.accountType = :type ORDER BY a.code")
    List<Account> findActiveByType(@Param("type") AccountType type);

    @Query("SELECT a FROM Account a WHERE a.parent IS NULL ORDER BY a.code")
    List<Account> findTopLevelAccounts();

    @Query("SELECT a FROM Account a WHERE a.parent.id = :parentId ORDER BY a.code")
    List<Account> findByParentId(@Param("parentId") Long parentId);

    @Query("SELECT DISTINCT a.accountType FROM Account a WHERE a.isActive = true ORDER BY a.accountType")
    List<AccountType> findDistinctAccountTypes();
}