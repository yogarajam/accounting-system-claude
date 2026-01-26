package com.accounting.service;

import com.accounting.exception.AccountingException;
import com.accounting.model.BankAccount;
import com.accounting.model.BankStatement;
import com.accounting.model.JournalEntryLine;
import com.accounting.repository.BankAccountRepository;
import com.accounting.repository.BankStatementRepository;
import com.accounting.repository.JournalEntryLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BankReconciliationService {

    private final BankAccountRepository bankAccountRepository;
    private final BankStatementRepository bankStatementRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;

    public List<BankAccount> findAllBankAccounts() {
        return bankAccountRepository.findAllWithRelations();
    }

    public List<BankAccount> findActiveBankAccounts() {
        return bankAccountRepository.findAllActive();
    }

    public Optional<BankAccount> findBankAccountById(Long id) {
        return bankAccountRepository.findByIdWithRelations(id);
    }

    @Transactional
    public BankAccount saveBankAccount(BankAccount bankAccount) {
        return bankAccountRepository.save(bankAccount);
    }

    public List<BankStatement> findStatementsByBankAccount(Long bankAccountId) {
        return bankStatementRepository.findByBankAccountId(bankAccountId);
    }

    public List<BankStatement> findUnreconciledStatements(Long bankAccountId) {
        return bankStatementRepository.findUnreconciledByBankAccountId(bankAccountId);
    }

    public List<BankStatement> findStatementsByDateRange(Long bankAccountId, LocalDate startDate, LocalDate endDate) {
        return bankStatementRepository.findByBankAccountIdAndDateRange(bankAccountId, startDate, endDate);
    }

    @Transactional
    public BankStatement importStatement(BankStatement statement) {
        return bankStatementRepository.save(statement);
    }

    @Transactional
    public List<BankStatement> importStatements(List<BankStatement> statements) {
        return bankStatementRepository.saveAll(statements);
    }

    @Transactional
    public void reconcileStatement(Long statementId, Long journalLineId) {
        BankStatement statement = bankStatementRepository.findById(statementId)
                .orElseThrow(() -> new AccountingException("Bank statement not found: " + statementId));

        JournalEntryLine journalLine = journalEntryLineRepository.findById(journalLineId)
                .orElseThrow(() -> new AccountingException("Journal entry line not found: " + journalLineId));

        BigDecimal statementAmount = statement.getNetAmount();
        BigDecimal journalAmount = journalLine.isDebit()
                ? journalLine.getDebitAmount()
                : journalLine.getCreditAmount().negate();

        if (statementAmount.compareTo(journalAmount) != 0) {
            throw new AccountingException("Statement amount does not match journal entry amount");
        }

        statement.setIsReconciled(true);
        statement.setMatchedJournalLine(journalLine);
        bankStatementRepository.save(statement);

        updateBankAccountBalance(statement.getBankAccount());
    }

    @Transactional
    public void unreconcileStatement(Long statementId) {
        BankStatement statement = bankStatementRepository.findById(statementId)
                .orElseThrow(() -> new AccountingException("Bank statement not found: " + statementId));

        statement.setIsReconciled(false);
        statement.setMatchedJournalLine(null);
        bankStatementRepository.save(statement);

        updateBankAccountBalance(statement.getBankAccount());
    }

    private void updateBankAccountBalance(BankAccount bankAccount) {
        BigDecimal reconciledBalance = bankStatementRepository.getReconciledBalance(bankAccount.getId());
        if (reconciledBalance == null) reconciledBalance = BigDecimal.ZERO;
        BigDecimal openingBalance = bankAccount.getOpeningBalance() != null ? bankAccount.getOpeningBalance() : BigDecimal.ZERO;
        bankAccount.setCurrentBalance(openingBalance.add(reconciledBalance));
        bankAccountRepository.save(bankAccount);
    }

    public BigDecimal getReconciledBalance(Long bankAccountId) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new AccountingException("Bank account not found: " + bankAccountId));

        BigDecimal reconciledBalance = bankStatementRepository.getReconciledBalance(bankAccountId);
        if (reconciledBalance == null) reconciledBalance = BigDecimal.ZERO;
        BigDecimal openingBalance = bankAccount.getOpeningBalance() != null ? bankAccount.getOpeningBalance() : BigDecimal.ZERO;
        return openingBalance.add(reconciledBalance);
    }

    public BigDecimal getUnreconciledDifference(Long bankAccountId) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new AccountingException("Bank account not found: " + bankAccountId));

        if (bankAccount.getGlAccount() == null) {
            throw new AccountingException("Bank account is not linked to a GL account");
        }

        BigDecimal glBalance = BigDecimal.ZERO;
        List<JournalEntryLine> lines = journalEntryLineRepository
                .findByAccountIdAndPosted(bankAccount.getGlAccount().getId());

        for (JournalEntryLine line : lines) {
            BigDecimal debit = line.getDebitAmount() != null ? line.getDebitAmount() : BigDecimal.ZERO;
            BigDecimal credit = line.getCreditAmount() != null ? line.getCreditAmount() : BigDecimal.ZERO;
            glBalance = glBalance.add(debit).subtract(credit);
        }

        BigDecimal reconciledBalance = getReconciledBalance(bankAccountId);

        return glBalance.subtract(reconciledBalance);
    }

    public Long countUnreconciled(Long bankAccountId) {
        return bankStatementRepository.countUnreconciled(bankAccountId);
    }

    public List<JournalEntryLine> findPotentialMatches(BankStatement statement) {
        if (statement.getBankAccount().getGlAccount() == null) {
            throw new AccountingException("Bank account is not linked to a GL account");
        }

        Long accountId = statement.getBankAccount().getGlAccount().getId();
        LocalDate startDate = statement.getTransactionDate().minusDays(7);
        LocalDate endDate = statement.getTransactionDate().plusDays(7);

        return journalEntryLineRepository.findByAccountIdAndPostedBetweenDates(accountId, startDate, endDate);
    }
}