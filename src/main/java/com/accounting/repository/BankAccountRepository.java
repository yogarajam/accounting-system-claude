package com.accounting.repository;

import com.accounting.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT ba FROM BankAccount ba WHERE ba.isActive = true")
    List<BankAccount> findAllActive();

    List<BankAccount> findByGlAccountId(Long glAccountId);
}