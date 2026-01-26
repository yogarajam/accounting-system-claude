package com.accounting.repository;

import com.accounting.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT ba FROM BankAccount ba LEFT JOIN FETCH ba.currency LEFT JOIN FETCH ba.glAccount WHERE ba.isActive = true")
    List<BankAccount> findAllActive();

    @Query("SELECT ba FROM BankAccount ba LEFT JOIN FETCH ba.currency LEFT JOIN FETCH ba.glAccount")
    List<BankAccount> findAllWithRelations();

    @Query("SELECT ba FROM BankAccount ba LEFT JOIN FETCH ba.currency LEFT JOIN FETCH ba.glAccount WHERE ba.id = :id")
    Optional<BankAccount> findByIdWithRelations(@Param("id") Long id);

    List<BankAccount> findByGlAccountId(Long glAccountId);
}