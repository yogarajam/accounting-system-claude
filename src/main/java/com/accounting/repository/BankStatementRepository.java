package com.accounting.repository;

import com.accounting.model.BankStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BankStatementRepository extends JpaRepository<BankStatement, Long> {

    List<BankStatement> findByBankAccountId(Long bankAccountId);

    @Query("SELECT bs FROM BankStatement bs WHERE bs.bankAccount.id = :bankAccountId " +
           "AND bs.isReconciled = false ORDER BY bs.transactionDate")
    List<BankStatement> findUnreconciledByBankAccountId(@Param("bankAccountId") Long bankAccountId);

    @Query("SELECT bs FROM BankStatement bs WHERE bs.bankAccount.id = :bankAccountId " +
           "AND bs.statementDate BETWEEN :startDate AND :endDate ORDER BY bs.transactionDate")
    List<BankStatement> findByBankAccountIdAndDateRange(
            @Param("bankAccountId") Long bankAccountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(bs.creditAmount) - SUM(bs.debitAmount), 0) FROM BankStatement bs " +
           "WHERE bs.bankAccount.id = :bankAccountId AND bs.isReconciled = true")
    BigDecimal getReconciledBalance(@Param("bankAccountId") Long bankAccountId);

    @Query("SELECT COUNT(bs) FROM BankStatement bs WHERE bs.bankAccount.id = :bankAccountId " +
           "AND bs.isReconciled = false")
    Long countUnreconciled(@Param("bankAccountId") Long bankAccountId);
}