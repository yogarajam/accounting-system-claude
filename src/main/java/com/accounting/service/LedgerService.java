package com.accounting.service;

import com.accounting.dto.LedgerDTO;
import com.accounting.exception.AccountingException;
import com.accounting.model.Account;
import com.accounting.model.JournalEntryLine;
import com.accounting.repository.AccountRepository;
import com.accounting.repository.JournalEntryLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final AccountRepository accountRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;

    public LedgerDTO generateLedger(Long accountId, LocalDate startDate, LocalDate endDate) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountingException("Account not found: " + accountId));

        LedgerDTO ledger = new LedgerDTO();
        ledger.setAccountId(account.getId());
        ledger.setAccountCode(account.getCode());
        ledger.setAccountName(account.getName());
        ledger.setAccountType(account.getAccountType().getDisplayName());
        ledger.setStartDate(startDate);
        ledger.setEndDate(endDate);

        BigDecimal openingDebit = journalEntryLineRepository.sumDebitByAccountIdBeforeDate(accountId, startDate);
        BigDecimal openingCredit = journalEntryLineRepository.sumCreditByAccountIdBeforeDate(accountId, startDate);

        // Handle null values from aggregate queries
        if (openingDebit == null) openingDebit = BigDecimal.ZERO;
        if (openingCredit == null) openingCredit = BigDecimal.ZERO;

        BigDecimal openingBalance;
        if (account.isDebitNormal()) {
            openingBalance = openingDebit.subtract(openingCredit);
        } else {
            openingBalance = openingCredit.subtract(openingDebit);
        }
        ledger.setOpeningBalance(openingBalance);

        List<JournalEntryLine> lines = journalEntryLineRepository
                .findByAccountIdAndPostedBetweenDates(accountId, startDate, endDate);

        BigDecimal runningBalance = openingBalance;

        for (JournalEntryLine line : lines) {
            LedgerDTO.LedgerEntryDTO entry = new LedgerDTO.LedgerEntryDTO();
            entry.setJournalEntryId(line.getJournalEntry().getId());
            entry.setEntryDate(line.getJournalEntry().getEntryDate());
            entry.setDate(line.getJournalEntry().getEntryDate());
            entry.setEntryNumber(line.getJournalEntry().getEntryNumber());
            entry.setDescription(line.getDescription() != null
                    ? line.getDescription()
                    : line.getJournalEntry().getDescription());
            entry.setReference(line.getJournalEntry().getReference());
            BigDecimal debitAmount = line.getDebitAmount() != null ? line.getDebitAmount() : BigDecimal.ZERO;
            BigDecimal creditAmount = line.getCreditAmount() != null ? line.getCreditAmount() : BigDecimal.ZERO;

            entry.setDebitAmount(debitAmount);
            entry.setCreditAmount(creditAmount);

            if (account.isDebitNormal()) {
                runningBalance = runningBalance.add(debitAmount).subtract(creditAmount);
            } else {
                runningBalance = runningBalance.add(creditAmount).subtract(debitAmount);
            }
            entry.setRunningBalance(runningBalance);

            ledger.addEntry(entry);
        }

        ledger.setClosingBalance(runningBalance);

        return ledger;
    }

    public LedgerDTO generateLedger(Long accountId) {
        LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
        LocalDate today = LocalDate.now();
        return generateLedger(accountId, startOfYear, today);
    }
}