package com.accounting.service;

import com.accounting.exception.AccountingException;
import com.accounting.model.Account;
import com.accounting.model.AccountType;
import com.accounting.repository.AccountRepository;
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
public class AccountService {

    private final AccountRepository accountRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public List<Account> findAllActive() {
        return accountRepository.findAllActive();
    }

    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    public Optional<Account> findByCode(String code) {
        return accountRepository.findByCode(code);
    }

    public List<Account> findByType(AccountType type) {
        return accountRepository.findByAccountType(type);
    }

    public List<Account> findActiveByType(AccountType type) {
        return accountRepository.findActiveByType(type);
    }

    public List<Account> findTopLevelAccounts() {
        return accountRepository.findTopLevelAccounts();
    }

    public List<Account> findByParentId(Long parentId) {
        return accountRepository.findByParentId(parentId);
    }

    @Transactional
    public Account save(Account account) {
        if (account.getId() == null && accountRepository.existsByCode(account.getCode())) {
            throw new AccountingException("Account code already exists: " + account.getCode());
        }

        return accountRepository.save(account);
    }

    @Transactional
    public void deactivate(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountingException("Account not found: " + id));

        BigDecimal balance = getBalance(id);
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new AccountingException("Cannot deactivate account with non-zero balance");
        }

        account.setIsActive(false);
        accountRepository.save(account);
    }

    @Transactional
    public void activate(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountingException("Account not found: " + id));

        account.setIsActive(true);
        accountRepository.save(account);
    }

    public BigDecimal getBalance(Long accountId) {
        BigDecimal totalDebit = journalEntryLineRepository.sumDebitByAccountId(accountId);
        BigDecimal totalCredit = journalEntryLineRepository.sumCreditByAccountId(accountId);

        // Handle null values from aggregate queries
        if (totalDebit == null) totalDebit = BigDecimal.ZERO;
        if (totalCredit == null) totalCredit = BigDecimal.ZERO;

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountingException("Account not found: " + accountId));

        if (account.isDebitNormal()) {
            return totalDebit.subtract(totalCredit);
        } else {
            return totalCredit.subtract(totalDebit);
        }
    }

    public BigDecimal getBalanceAsOfDate(Long accountId, LocalDate asOfDate) {
        LocalDate startOfTime = LocalDate.of(1900, 1, 1);
        BigDecimal totalDebit = journalEntryLineRepository.sumDebitByAccountIdBetweenDates(
                accountId, startOfTime, asOfDate);
        BigDecimal totalCredit = journalEntryLineRepository.sumCreditByAccountIdBetweenDates(
                accountId, startOfTime, asOfDate);

        // Handle null values from aggregate queries
        if (totalDebit == null) totalDebit = BigDecimal.ZERO;
        if (totalCredit == null) totalCredit = BigDecimal.ZERO;

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountingException("Account not found: " + accountId));

        if (account.isDebitNormal()) {
            return totalDebit.subtract(totalCredit);
        } else {
            return totalCredit.subtract(totalDebit);
        }
    }

    public BigDecimal getBalanceBetweenDates(Long accountId, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalDebit = journalEntryLineRepository.sumDebitByAccountIdBetweenDates(
                accountId, startDate, endDate);
        BigDecimal totalCredit = journalEntryLineRepository.sumCreditByAccountIdBetweenDates(
                accountId, startDate, endDate);

        // Handle null values from aggregate queries
        if (totalDebit == null) totalDebit = BigDecimal.ZERO;
        if (totalCredit == null) totalCredit = BigDecimal.ZERO;

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountingException("Account not found: " + accountId));

        if (account.isDebitNormal()) {
            return totalDebit.subtract(totalCredit);
        } else {
            return totalCredit.subtract(totalDebit);
        }
    }

    @Transactional
    public Account createAccountIfNotExists(String code, String name, AccountType type, String description) {
        return accountRepository.findByCode(code)
                .orElseGet(() -> {
                    Account account = new Account();
                    account.setCode(code);
                    account.setName(name);
                    account.setAccountType(type);
                    account.setDescription(description);
                    account.setIsActive(true);
                    return accountRepository.save(account);
                });
    }
}