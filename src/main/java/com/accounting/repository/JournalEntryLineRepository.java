package com.accounting.repository;

import com.accounting.model.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {

    @Query("SELECT jl FROM JournalEntryLine jl " +
           "JOIN jl.journalEntry je " +
           "WHERE jl.account.id = :accountId " +
           "AND je.status = 'POSTED' " +
           "ORDER BY je.entryDate, je.entryNumber")
    List<JournalEntryLine> findByAccountIdAndPosted(@Param("accountId") Long accountId);

    @Query("SELECT jl FROM JournalEntryLine jl " +
           "JOIN jl.journalEntry je " +
           "WHERE jl.account.id = :accountId " +
           "AND je.status = 'POSTED' " +
           "AND je.entryDate BETWEEN :startDate AND :endDate " +
           "ORDER BY je.entryDate, je.entryNumber")
    List<JournalEntryLine> findByAccountIdAndPostedBetweenDates(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(jl.debitAmount), 0) FROM JournalEntryLine jl " +
           "JOIN jl.journalEntry je " +
           "WHERE jl.account.id = :accountId AND je.status = 'POSTED'")
    BigDecimal sumDebitByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT COALESCE(SUM(jl.creditAmount), 0) FROM JournalEntryLine jl " +
           "JOIN jl.journalEntry je " +
           "WHERE jl.account.id = :accountId AND je.status = 'POSTED'")
    BigDecimal sumCreditByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT COALESCE(SUM(jl.debitAmount), 0) FROM JournalEntryLine jl " +
           "JOIN jl.journalEntry je " +
           "WHERE jl.account.id = :accountId AND je.status = 'POSTED' " +
           "AND je.entryDate BETWEEN :startDate AND :endDate")
    BigDecimal sumDebitByAccountIdBetweenDates(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(jl.creditAmount), 0) FROM JournalEntryLine jl " +
           "JOIN jl.journalEntry je " +
           "WHERE jl.account.id = :accountId AND je.status = 'POSTED' " +
           "AND je.entryDate BETWEEN :startDate AND :endDate")
    BigDecimal sumCreditByAccountIdBetweenDates(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(jl.debitAmount), 0) FROM JournalEntryLine jl " +
           "JOIN jl.journalEntry je " +
           "WHERE jl.account.id = :accountId AND je.status = 'POSTED' " +
           "AND je.entryDate < :date")
    BigDecimal sumDebitByAccountIdBeforeDate(
            @Param("accountId") Long accountId,
            @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(jl.creditAmount), 0) FROM JournalEntryLine jl " +
           "JOIN jl.journalEntry je " +
           "WHERE jl.account.id = :accountId AND je.status = 'POSTED' " +
           "AND je.entryDate < :date")
    BigDecimal sumCreditByAccountIdBeforeDate(
            @Param("accountId") Long accountId,
            @Param("date") LocalDate date);
}