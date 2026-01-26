package com.accounting.service;

import com.accounting.dto.LedgerDTO;
import com.accounting.exception.AccountingException;
import com.accounting.model.*;
import com.accounting.repository.AccountRepository;
import com.accounting.repository.JournalEntryLineRepository;
import com.accounting.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LedgerService Unit Tests")
class LedgerServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JournalEntryLineRepository journalEntryLineRepository;

    @InjectMocks
    private LedgerService ledgerService;

    private Account cashAccount;
    private Account revenueAccount;
    private JournalEntry journalEntry;
    private JournalEntryLine debitLine;
    private JournalEntryLine creditLine;

    @BeforeEach
    void setUp() {
        cashAccount = TestDataBuilder.createCashAccount();
        revenueAccount = TestDataBuilder.createSalesRevenue();

        journalEntry = TestDataBuilder.createJournalEntry(1L, "JE-202601-0001", EntryStatus.POSTED);
        journalEntry.setEntryDate(LocalDate.now().minusDays(5));
        journalEntry.setDescription("Sales revenue");

        debitLine = new JournalEntryLine();
        debitLine.setId(1L);
        debitLine.setJournalEntry(journalEntry);
        debitLine.setAccount(cashAccount);
        debitLine.setDebitAmount(BigDecimal.valueOf(1000));
        debitLine.setCreditAmount(BigDecimal.ZERO);
        debitLine.setDescription("Cash received");

        creditLine = new JournalEntryLine();
        creditLine.setId(2L);
        creditLine.setJournalEntry(journalEntry);
        creditLine.setAccount(revenueAccount);
        creditLine.setDebitAmount(BigDecimal.ZERO);
        creditLine.setCreditAmount(BigDecimal.valueOf(1000));
        creditLine.setDescription("Revenue recognized");
    }

    @Nested
    @DisplayName("Generate Ledger with Date Range")
    class GenerateLedgerWithDateRange {

        @Test
        @DisplayName("Should generate ledger for debit-normal account")
        void generateLedger_DebitNormalAccount_ReturnsCorrectLedger() {
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(journalEntryLineRepository.sumDebitByAccountIdBeforeDate(1L, startDate))
                    .thenReturn(BigDecimal.valueOf(500));
            when(journalEntryLineRepository.sumCreditByAccountIdBeforeDate(1L, startDate))
                    .thenReturn(BigDecimal.valueOf(100));
            when(journalEntryLineRepository.findByAccountIdAndPostedBetweenDates(1L, startDate, endDate))
                    .thenReturn(Collections.singletonList(debitLine));

            LedgerDTO result = ledgerService.generateLedger(1L, startDate, endDate);

            assertThat(result.getAccountId()).isEqualTo(1L);
            assertThat(result.getAccountCode()).isEqualTo("1000");
            assertThat(result.getAccountName()).isEqualTo("Cash");
            assertThat(result.getOpeningBalance()).isEqualByComparingTo(BigDecimal.valueOf(400)); // 500 - 100
            assertThat(result.getEntries()).hasSize(1);
            assertThat(result.getClosingBalance()).isEqualByComparingTo(BigDecimal.valueOf(1400)); // 400 + 1000
        }

        @Test
        @DisplayName("Should generate ledger for credit-normal account")
        void generateLedger_CreditNormalAccount_ReturnsCorrectLedger() {
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            when(accountRepository.findById(4L)).thenReturn(Optional.of(revenueAccount));
            when(journalEntryLineRepository.sumDebitByAccountIdBeforeDate(4L, startDate))
                    .thenReturn(BigDecimal.valueOf(100));
            when(journalEntryLineRepository.sumCreditByAccountIdBeforeDate(4L, startDate))
                    .thenReturn(BigDecimal.valueOf(5000));
            when(journalEntryLineRepository.findByAccountIdAndPostedBetweenDates(4L, startDate, endDate))
                    .thenReturn(Collections.singletonList(creditLine));

            LedgerDTO result = ledgerService.generateLedger(4L, startDate, endDate);

            assertThat(result.getAccountId()).isEqualTo(4L);
            assertThat(result.getAccountCode()).isEqualTo("4000");
            assertThat(result.getAccountName()).isEqualTo("Sales Revenue");
            assertThat(result.getOpeningBalance()).isEqualByComparingTo(BigDecimal.valueOf(4900)); // 5000 - 100
            assertThat(result.getClosingBalance()).isEqualByComparingTo(BigDecimal.valueOf(5900)); // 4900 + 1000
        }

        @Test
        @DisplayName("Should throw exception when account not found")
        void generateLedger_AccountNotFound_ThrowsException() {
            when(accountRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ledgerService.generateLedger(999L, LocalDate.now().minusDays(30), LocalDate.now()))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Account not found");
        }

        @Test
        @DisplayName("Should handle empty transactions")
        void generateLedger_NoTransactions_ReturnsEmptyLedger() {
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(journalEntryLineRepository.sumDebitByAccountIdBeforeDate(1L, startDate))
                    .thenReturn(BigDecimal.ZERO);
            when(journalEntryLineRepository.sumCreditByAccountIdBeforeDate(1L, startDate))
                    .thenReturn(BigDecimal.ZERO);
            when(journalEntryLineRepository.findByAccountIdAndPostedBetweenDates(1L, startDate, endDate))
                    .thenReturn(Collections.emptyList());

            LedgerDTO result = ledgerService.generateLedger(1L, startDate, endDate);

            assertThat(result.getOpeningBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getClosingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getEntries()).isEmpty();
        }

        @Test
        @DisplayName("Should calculate running balance correctly for multiple entries")
        void generateLedger_MultipleEntries_CalculatesRunningBalance() {
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            JournalEntry entry2 = TestDataBuilder.createJournalEntry(2L, "JE-202601-0002", EntryStatus.POSTED);
            entry2.setEntryDate(LocalDate.now().minusDays(2));
            entry2.setDescription("Another sale");

            JournalEntryLine debitLine2 = new JournalEntryLine();
            debitLine2.setId(3L);
            debitLine2.setJournalEntry(entry2);
            debitLine2.setAccount(cashAccount);
            debitLine2.setDebitAmount(BigDecimal.valueOf(500));
            debitLine2.setCreditAmount(BigDecimal.ZERO);

            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(journalEntryLineRepository.sumDebitByAccountIdBeforeDate(1L, startDate))
                    .thenReturn(BigDecimal.valueOf(1000));
            when(journalEntryLineRepository.sumCreditByAccountIdBeforeDate(1L, startDate))
                    .thenReturn(BigDecimal.ZERO);
            when(journalEntryLineRepository.findByAccountIdAndPostedBetweenDates(1L, startDate, endDate))
                    .thenReturn(Arrays.asList(debitLine, debitLine2));

            LedgerDTO result = ledgerService.generateLedger(1L, startDate, endDate);

            assertThat(result.getOpeningBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000));
            assertThat(result.getEntries()).hasSize(2);
            // First entry: 1000 + 1000 = 2000
            assertThat(result.getEntries().get(0).getRunningBalance())
                    .isEqualByComparingTo(BigDecimal.valueOf(2000));
            // Second entry: 2000 + 500 = 2500
            assertThat(result.getEntries().get(1).getRunningBalance())
                    .isEqualByComparingTo(BigDecimal.valueOf(2500));
            assertThat(result.getClosingBalance()).isEqualByComparingTo(BigDecimal.valueOf(2500));
        }
    }

    @Nested
    @DisplayName("Generate Ledger with Default Dates")
    class GenerateLedgerDefaultDates {

        @Test
        @DisplayName("Should use current year start and today as default dates")
        void generateLedger_NoDateRange_UsesDefaultDates() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(journalEntryLineRepository.sumDebitByAccountIdBeforeDate(anyLong(), any(LocalDate.class)))
                    .thenReturn(BigDecimal.ZERO);
            when(journalEntryLineRepository.sumCreditByAccountIdBeforeDate(anyLong(), any(LocalDate.class)))
                    .thenReturn(BigDecimal.ZERO);
            when(journalEntryLineRepository.findByAccountIdAndPostedBetweenDates(
                    anyLong(), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            LedgerDTO result = ledgerService.generateLedger(1L);

            assertThat(result.getStartDate()).isEqualTo(LocalDate.now().withDayOfYear(1));
            assertThat(result.getEndDate()).isEqualTo(LocalDate.now());
        }
    }

    @Nested
    @DisplayName("Ledger Entry Details")
    class LedgerEntryDetails {

        @Test
        @DisplayName("Should populate entry details correctly")
        void generateLedger_PopulatesEntryDetails() {
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            journalEntry.setReference("INV-001");

            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(journalEntryLineRepository.sumDebitByAccountIdBeforeDate(1L, startDate))
                    .thenReturn(BigDecimal.ZERO);
            when(journalEntryLineRepository.sumCreditByAccountIdBeforeDate(1L, startDate))
                    .thenReturn(BigDecimal.ZERO);
            when(journalEntryLineRepository.findByAccountIdAndPostedBetweenDates(1L, startDate, endDate))
                    .thenReturn(Collections.singletonList(debitLine));

            LedgerDTO result = ledgerService.generateLedger(1L, startDate, endDate);

            LedgerDTO.LedgerEntryDTO entry = result.getEntries().get(0);
            assertThat(entry.getJournalEntryId()).isEqualTo(1L);
            assertThat(entry.getEntryNumber()).isEqualTo("JE-202601-0001");
            assertThat(entry.getDescription()).isEqualTo("Cash received");
            assertThat(entry.getReference()).isEqualTo("INV-001");
            assertThat(entry.getDebitAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
            assertThat(entry.getCreditAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should use journal entry description if line description is null")
        void generateLedger_NullLineDescription_UsesEntryDescription() {
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            debitLine.setDescription(null);

            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(journalEntryLineRepository.sumDebitByAccountIdBeforeDate(1L, startDate))
                    .thenReturn(BigDecimal.ZERO);
            when(journalEntryLineRepository.sumCreditByAccountIdBeforeDate(1L, startDate))
                    .thenReturn(BigDecimal.ZERO);
            when(journalEntryLineRepository.findByAccountIdAndPostedBetweenDates(1L, startDate, endDate))
                    .thenReturn(Collections.singletonList(debitLine));

            LedgerDTO result = ledgerService.generateLedger(1L, startDate, endDate);

            assertThat(result.getEntries().get(0).getDescription()).isEqualTo("Sales revenue");
        }
    }

    @Nested
    @DisplayName("Ledger Totals")
    class LedgerTotals {

        @Test
        @DisplayName("Should calculate total debits correctly")
        void getTotalDebits_ReturnsCorrectSum() {
            LedgerDTO ledger = new LedgerDTO();

            LedgerDTO.LedgerEntryDTO entry1 = new LedgerDTO.LedgerEntryDTO();
            entry1.setDebitAmount(BigDecimal.valueOf(100));
            entry1.setCreditAmount(BigDecimal.ZERO);

            LedgerDTO.LedgerEntryDTO entry2 = new LedgerDTO.LedgerEntryDTO();
            entry2.setDebitAmount(BigDecimal.valueOf(250));
            entry2.setCreditAmount(BigDecimal.ZERO);

            ledger.addEntry(entry1);
            ledger.addEntry(entry2);

            assertThat(ledger.getTotalDebits()).isEqualByComparingTo(BigDecimal.valueOf(350));
        }

        @Test
        @DisplayName("Should calculate total credits correctly")
        void getTotalCredits_ReturnsCorrectSum() {
            LedgerDTO ledger = new LedgerDTO();

            LedgerDTO.LedgerEntryDTO entry1 = new LedgerDTO.LedgerEntryDTO();
            entry1.setDebitAmount(BigDecimal.ZERO);
            entry1.setCreditAmount(BigDecimal.valueOf(500));

            LedgerDTO.LedgerEntryDTO entry2 = new LedgerDTO.LedgerEntryDTO();
            entry2.setDebitAmount(BigDecimal.ZERO);
            entry2.setCreditAmount(BigDecimal.valueOf(300));

            ledger.addEntry(entry1);
            ledger.addEntry(entry2);

            assertThat(ledger.getTotalCredits()).isEqualByComparingTo(BigDecimal.valueOf(800));
        }
    }
}