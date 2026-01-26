package com.accounting.service;

import com.accounting.dto.BalanceSheetDTO;
import com.accounting.dto.DashboardDTO;
import com.accounting.dto.ProfitLossDTO;
import com.accounting.dto.TrialBalanceDTO;
import com.accounting.model.*;
import com.accounting.repository.AccountRepository;
import com.accounting.repository.InvoiceRepository;
import com.accounting.repository.JournalEntryRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService Unit Tests")
class ReportServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private ReportService reportService;

    private Account cashAccount;
    private Account arAccount;
    private Account apAccount;
    private Account revenueAccount;
    private Account expenseAccount;
    private Account equityAccount;

    @BeforeEach
    void setUp() {
        cashAccount = TestDataBuilder.createCashAccount();
        arAccount = TestDataBuilder.createAccountsReceivable();
        apAccount = TestDataBuilder.createAccountsPayable();
        revenueAccount = TestDataBuilder.createSalesRevenue();
        expenseAccount = TestDataBuilder.createExpenseAccount();
        equityAccount = TestDataBuilder.createEquityAccount();
    }

    @Nested
    @DisplayName("Trial Balance Report")
    class TrialBalanceReport {

        @Test
        @DisplayName("Should generate trial balance with active accounts")
        void generateTrialBalance_WithActiveAccounts_ReturnsTrialBalance() {
            LocalDate asOfDate = LocalDate.now();
            when(accountRepository.findAllActive())
                    .thenReturn(Arrays.asList(cashAccount, revenueAccount));
            when(accountService.getBalanceAsOfDate(1L, asOfDate))
                    .thenReturn(BigDecimal.valueOf(1000));
            when(accountService.getBalanceAsOfDate(4L, asOfDate))
                    .thenReturn(BigDecimal.valueOf(1000));

            TrialBalanceDTO result = reportService.generateTrialBalance(asOfDate);

            assertThat(result.getAsOfDate()).isEqualTo(asOfDate);
            assertThat(result.getLines()).hasSize(2);
            assertThat(result.isBalanced()).isTrue();
        }

        @Test
        @DisplayName("Should exclude accounts with zero balance from trial balance")
        void generateTrialBalance_ExcludesZeroBalanceAccounts() {
            LocalDate asOfDate = LocalDate.now();
            when(accountRepository.findAllActive())
                    .thenReturn(Arrays.asList(cashAccount, revenueAccount));
            when(accountService.getBalanceAsOfDate(1L, asOfDate))
                    .thenReturn(BigDecimal.valueOf(1000));
            when(accountService.getBalanceAsOfDate(4L, asOfDate))
                    .thenReturn(BigDecimal.ZERO);

            TrialBalanceDTO result = reportService.generateTrialBalance(asOfDate);

            assertThat(result.getLines()).hasSize(1);
            assertThat(result.getLines().get(0).getAccountCode()).isEqualTo("1000");
        }

        @Test
        @DisplayName("Should return empty trial balance when no active accounts")
        void generateTrialBalance_NoActiveAccounts_ReturnsEmpty() {
            LocalDate asOfDate = LocalDate.now();
            when(accountRepository.findAllActive()).thenReturn(Collections.emptyList());

            TrialBalanceDTO result = reportService.generateTrialBalance(asOfDate);

            assertThat(result.getLines()).isEmpty();
            assertThat(result.getTotalDebit()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getTotalCredit()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should correctly categorize debit and credit balances")
        void generateTrialBalance_CorrectlyCategorizesBalances() {
            LocalDate asOfDate = LocalDate.now();
            when(accountRepository.findAllActive())
                    .thenReturn(Arrays.asList(cashAccount, apAccount));
            when(accountService.getBalanceAsOfDate(1L, asOfDate))
                    .thenReturn(BigDecimal.valueOf(5000)); // Debit normal - positive = debit
            when(accountService.getBalanceAsOfDate(3L, asOfDate))
                    .thenReturn(BigDecimal.valueOf(5000)); // Credit normal - positive = credit

            TrialBalanceDTO result = reportService.generateTrialBalance(asOfDate);

            assertThat(result.getTotalDebit()).isEqualByComparingTo(BigDecimal.valueOf(5000));
            assertThat(result.getTotalCredit()).isEqualByComparingTo(BigDecimal.valueOf(5000));
            assertThat(result.isBalanced()).isTrue();
        }
    }

    @Nested
    @DisplayName("Profit & Loss Report")
    class ProfitLossReport {

        @Test
        @DisplayName("Should generate profit and loss statement")
        void generateProfitLoss_WithRevenueAndExpenses_ReturnsProfitLoss() {
            LocalDate startDate = LocalDate.now().withDayOfYear(1);
            LocalDate endDate = LocalDate.now();

            when(accountRepository.findActiveByType(AccountType.REVENUE))
                    .thenReturn(Arrays.asList(revenueAccount));
            when(accountRepository.findActiveByType(AccountType.EXPENSE))
                    .thenReturn(Arrays.asList(expenseAccount));
            when(accountService.getBalanceBetweenDates(4L, startDate, endDate))
                    .thenReturn(BigDecimal.valueOf(10000));
            when(accountService.getBalanceBetweenDates(5L, startDate, endDate))
                    .thenReturn(BigDecimal.valueOf(3000));

            ProfitLossDTO result = reportService.generateProfitLoss(startDate, endDate);

            assertThat(result.getStartDate()).isEqualTo(startDate);
            assertThat(result.getEndDate()).isEqualTo(endDate);
            assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(10000));
            assertThat(result.getTotalExpenses()).isEqualByComparingTo(BigDecimal.valueOf(3000));
            assertThat(result.getNetIncome()).isEqualByComparingTo(BigDecimal.valueOf(7000));
            assertThat(result.isProfitable()).isTrue();
        }

        @Test
        @DisplayName("Should show loss when expenses exceed revenue")
        void generateProfitLoss_ExpensesExceedRevenue_ShowsLoss() {
            LocalDate startDate = LocalDate.now().withDayOfYear(1);
            LocalDate endDate = LocalDate.now();

            when(accountRepository.findActiveByType(AccountType.REVENUE))
                    .thenReturn(Arrays.asList(revenueAccount));
            when(accountRepository.findActiveByType(AccountType.EXPENSE))
                    .thenReturn(Arrays.asList(expenseAccount));
            when(accountService.getBalanceBetweenDates(4L, startDate, endDate))
                    .thenReturn(BigDecimal.valueOf(5000));
            when(accountService.getBalanceBetweenDates(5L, startDate, endDate))
                    .thenReturn(BigDecimal.valueOf(8000));

            ProfitLossDTO result = reportService.generateProfitLoss(startDate, endDate);

            assertThat(result.getNetIncome()).isEqualByComparingTo(BigDecimal.valueOf(-3000));
            assertThat(result.isProfitable()).isFalse();
        }

        @Test
        @DisplayName("Should exclude zero balance accounts from P&L")
        void generateProfitLoss_ExcludesZeroBalanceAccounts() {
            LocalDate startDate = LocalDate.now().withDayOfYear(1);
            LocalDate endDate = LocalDate.now();

            Account secondRevenueAccount = TestDataBuilder.createAccount(10L, "4100", "Service Revenue", AccountType.REVENUE);

            when(accountRepository.findActiveByType(AccountType.REVENUE))
                    .thenReturn(Arrays.asList(revenueAccount, secondRevenueAccount));
            when(accountRepository.findActiveByType(AccountType.EXPENSE))
                    .thenReturn(Collections.emptyList());
            when(accountService.getBalanceBetweenDates(4L, startDate, endDate))
                    .thenReturn(BigDecimal.valueOf(5000));
            when(accountService.getBalanceBetweenDates(10L, startDate, endDate))
                    .thenReturn(BigDecimal.ZERO);

            ProfitLossDTO result = reportService.generateProfitLoss(startDate, endDate);

            assertThat(result.getRevenueAccounts()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Balance Sheet Report")
    class BalanceSheetReport {

        @Test
        @DisplayName("Should generate balance sheet with all account types")
        void generateBalanceSheet_AllAccountTypes_ReturnsBalanceSheet() {
            LocalDate asOfDate = LocalDate.now();

            when(accountRepository.findActiveByType(AccountType.ASSET))
                    .thenReturn(Arrays.asList(cashAccount, arAccount));
            when(accountRepository.findActiveByType(AccountType.LIABILITY))
                    .thenReturn(Arrays.asList(apAccount));
            when(accountRepository.findActiveByType(AccountType.EQUITY))
                    .thenReturn(Arrays.asList(equityAccount));
            when(accountRepository.findActiveByType(AccountType.REVENUE))
                    .thenReturn(Arrays.asList(revenueAccount));
            when(accountRepository.findActiveByType(AccountType.EXPENSE))
                    .thenReturn(Arrays.asList(expenseAccount));

            when(accountService.getBalanceAsOfDate(eq(1L), eq(asOfDate)))
                    .thenReturn(BigDecimal.valueOf(5000));
            when(accountService.getBalanceAsOfDate(eq(2L), eq(asOfDate)))
                    .thenReturn(BigDecimal.valueOf(3000));
            when(accountService.getBalanceAsOfDate(eq(3L), eq(asOfDate)))
                    .thenReturn(BigDecimal.valueOf(2000));
            when(accountService.getBalanceAsOfDate(eq(6L), eq(asOfDate)))
                    .thenReturn(BigDecimal.valueOf(4000));

            // Mock for P&L calculation (retained earnings)
            when(accountService.getBalanceBetweenDates(eq(4L), any(), eq(asOfDate)))
                    .thenReturn(BigDecimal.valueOf(3000));
            when(accountService.getBalanceBetweenDates(eq(5L), any(), eq(asOfDate)))
                    .thenReturn(BigDecimal.valueOf(1000));

            BalanceSheetDTO result = reportService.generateBalanceSheet(asOfDate);

            assertThat(result.getAsOfDate()).isEqualTo(asOfDate);
            assertThat(result.getAssetAccounts()).hasSize(2);
            assertThat(result.getLiabilityAccounts()).hasSize(1);
            assertThat(result.getEquityAccounts()).hasSize(1);
            assertThat(result.getTotalAssets()).isEqualByComparingTo(BigDecimal.valueOf(8000));
            assertThat(result.getTotalLiabilities()).isEqualByComparingTo(BigDecimal.valueOf(2000));
            assertThat(result.getTotalEquity()).isEqualByComparingTo(BigDecimal.valueOf(4000));
            assertThat(result.getRetainedEarnings()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        }

        @Test
        @DisplayName("Should exclude zero balance accounts from balance sheet")
        void generateBalanceSheet_ExcludesZeroBalanceAccounts() {
            LocalDate asOfDate = LocalDate.now();

            when(accountRepository.findActiveByType(AccountType.ASSET))
                    .thenReturn(Arrays.asList(cashAccount));
            when(accountRepository.findActiveByType(AccountType.LIABILITY))
                    .thenReturn(Collections.emptyList());
            when(accountRepository.findActiveByType(AccountType.EQUITY))
                    .thenReturn(Collections.emptyList());
            when(accountRepository.findActiveByType(AccountType.REVENUE))
                    .thenReturn(Collections.emptyList());
            when(accountRepository.findActiveByType(AccountType.EXPENSE))
                    .thenReturn(Collections.emptyList());

            when(accountService.getBalanceAsOfDate(1L, asOfDate))
                    .thenReturn(BigDecimal.ZERO);

            BalanceSheetDTO result = reportService.generateBalanceSheet(asOfDate);

            assertThat(result.getAssetAccounts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Dashboard Report")
    class DashboardReport {

        @Test
        @DisplayName("Should generate dashboard with all metrics")
        void generateDashboard_ReturnsAllMetrics() {
            when(accountRepository.findActiveByType(AccountType.ASSET))
                    .thenReturn(Arrays.asList(cashAccount, arAccount));
            when(accountRepository.findActiveByType(AccountType.LIABILITY))
                    .thenReturn(Arrays.asList(apAccount));
            when(accountRepository.findActiveByType(AccountType.EQUITY))
                    .thenReturn(Arrays.asList(equityAccount));
            when(accountRepository.findActiveByType(AccountType.REVENUE))
                    .thenReturn(Arrays.asList(revenueAccount));
            when(accountRepository.findActiveByType(AccountType.EXPENSE))
                    .thenReturn(Arrays.asList(expenseAccount));

            when(accountService.getBalance(1L)).thenReturn(BigDecimal.valueOf(10000));
            when(accountService.getBalance(2L)).thenReturn(BigDecimal.valueOf(5000));
            when(accountService.getBalance(3L)).thenReturn(BigDecimal.valueOf(3000));
            when(accountService.getBalance(6L)).thenReturn(BigDecimal.valueOf(12000));

            when(accountService.getBalanceBetweenDates(eq(4L), any(), any()))
                    .thenReturn(BigDecimal.valueOf(20000));
            when(accountService.getBalanceBetweenDates(eq(5L), any(), any()))
                    .thenReturn(BigDecimal.valueOf(8000));

            when(accountRepository.findByCode("1000")).thenReturn(Optional.of(cashAccount));
            when(accountRepository.findByCode("1200")).thenReturn(Optional.of(arAccount));
            when(accountRepository.findByCode("2000")).thenReturn(Optional.of(apAccount));

            when(journalEntryRepository.countByStatus(EntryStatus.DRAFT)).thenReturn(5L);
            when(invoiceRepository.findOverdueInvoices(any())).thenReturn(Collections.emptyList());
            when(invoiceRepository.sumTotalByStatus(InvoiceStatus.OVERDUE)).thenReturn(BigDecimal.valueOf(2500));

            DashboardDTO result = reportService.generateDashboard();

            assertThat(result.getTotalAssets()).isEqualByComparingTo(BigDecimal.valueOf(15000));
            assertThat(result.getTotalLiabilities()).isEqualByComparingTo(BigDecimal.valueOf(3000));
            assertThat(result.getTotalEquity()).isEqualByComparingTo(BigDecimal.valueOf(12000));
            assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(20000));
            assertThat(result.getTotalExpenses()).isEqualByComparingTo(BigDecimal.valueOf(8000));
            assertThat(result.getNetIncome()).isEqualByComparingTo(BigDecimal.valueOf(12000));
            assertThat(result.getCashBalance()).isEqualByComparingTo(BigDecimal.valueOf(10000));
            assertThat(result.getAccountsReceivable()).isEqualByComparingTo(BigDecimal.valueOf(5000));
            assertThat(result.getAccountsPayable()).isEqualByComparingTo(BigDecimal.valueOf(3000));
            assertThat(result.getPendingJournalEntries()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Should handle missing standard accounts gracefully")
        void generateDashboard_MissingAccounts_HandlesGracefully() {
            when(accountRepository.findActiveByType(any())).thenReturn(Collections.emptyList());
            when(accountRepository.findByCode(anyString())).thenReturn(Optional.empty());
            when(journalEntryRepository.countByStatus(EntryStatus.DRAFT)).thenReturn(0L);
            when(invoiceRepository.findOverdueInvoices(any())).thenReturn(Collections.emptyList());
            when(invoiceRepository.sumTotalByStatus(InvoiceStatus.OVERDUE)).thenReturn(null);

            DashboardDTO result = reportService.generateDashboard();

            assertThat(result.getTotalAssets()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getCashBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}