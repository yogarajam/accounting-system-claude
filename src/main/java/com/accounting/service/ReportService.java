package com.accounting.service;

import com.accounting.dto.BalanceSheetDTO;
import com.accounting.dto.DashboardDTO;
import com.accounting.dto.ProfitLossDTO;
import com.accounting.dto.TrialBalanceDTO;
import com.accounting.model.Account;
import com.accounting.model.AccountType;
import com.accounting.model.EntryStatus;
import com.accounting.model.InvoiceStatus;
import com.accounting.repository.AccountRepository;
import com.accounting.repository.InvoiceRepository;
import com.accounting.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final JournalEntryRepository journalEntryRepository;
    private final InvoiceRepository invoiceRepository;

    public TrialBalanceDTO generateTrialBalance(LocalDate asOfDate) {
        TrialBalanceDTO trialBalance = new TrialBalanceDTO();
        trialBalance.setAsOfDate(asOfDate);

        List<Account> accounts = accountRepository.findAllActive();

        for (Account account : accounts) {
            BigDecimal balance = accountService.getBalanceAsOfDate(account.getId(), asOfDate);

            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                TrialBalanceDTO.TrialBalanceLineDTO line = new TrialBalanceDTO.TrialBalanceLineDTO(
                        account.getId(),
                        account.getCode(),
                        account.getName(),
                        account.getAccountType(),
                        balance
                );
                trialBalance.addLine(line);
            }
        }

        return trialBalance;
    }

    public ProfitLossDTO generateProfitLoss(LocalDate startDate, LocalDate endDate) {
        ProfitLossDTO profitLoss = new ProfitLossDTO();
        profitLoss.setStartDate(startDate);
        profitLoss.setEndDate(endDate);

        List<Account> revenueAccounts = accountRepository.findActiveByType(AccountType.REVENUE);
        for (Account account : revenueAccounts) {
            BigDecimal balance = accountService.getBalanceBetweenDates(account.getId(), startDate, endDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                profitLoss.addRevenueAccount(new ProfitLossDTO.AccountBalanceDTO(
                        account.getId(),
                        account.getCode(),
                        account.getName(),
                        balance
                ));
            }
        }

        List<Account> expenseAccounts = accountRepository.findActiveByType(AccountType.EXPENSE);
        for (Account account : expenseAccounts) {
            BigDecimal balance = accountService.getBalanceBetweenDates(account.getId(), startDate, endDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                profitLoss.addExpenseAccount(new ProfitLossDTO.AccountBalanceDTO(
                        account.getId(),
                        account.getCode(),
                        account.getName(),
                        balance
                ));
            }
        }

        profitLoss.calculateNetIncome();
        return profitLoss;
    }

    public BalanceSheetDTO generateBalanceSheet(LocalDate asOfDate) {
        BalanceSheetDTO balanceSheet = new BalanceSheetDTO();
        balanceSheet.setAsOfDate(asOfDate);

        List<Account> assetAccounts = accountRepository.findActiveByType(AccountType.ASSET);
        for (Account account : assetAccounts) {
            BigDecimal balance = accountService.getBalanceAsOfDate(account.getId(), asOfDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                balanceSheet.addAssetAccount(new BalanceSheetDTO.AccountBalanceDTO(
                        account.getId(),
                        account.getCode(),
                        account.getName(),
                        balance
                ));
            }
        }

        List<Account> liabilityAccounts = accountRepository.findActiveByType(AccountType.LIABILITY);
        for (Account account : liabilityAccounts) {
            BigDecimal balance = accountService.getBalanceAsOfDate(account.getId(), asOfDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                balanceSheet.addLiabilityAccount(new BalanceSheetDTO.AccountBalanceDTO(
                        account.getId(),
                        account.getCode(),
                        account.getName(),
                        balance
                ));
            }
        }

        List<Account> equityAccounts = accountRepository.findActiveByType(AccountType.EQUITY);
        for (Account account : equityAccounts) {
            BigDecimal balance = accountService.getBalanceAsOfDate(account.getId(), asOfDate);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                balanceSheet.addEquityAccount(new BalanceSheetDTO.AccountBalanceDTO(
                        account.getId(),
                        account.getCode(),
                        account.getName(),
                        balance
                ));
            }
        }

        LocalDate fiscalYearStart = asOfDate.withDayOfYear(1);
        ProfitLossDTO profitLoss = generateProfitLoss(fiscalYearStart, asOfDate);
        balanceSheet.setRetainedEarnings(profitLoss.getNetIncome());

        return balanceSheet;
    }

    public DashboardDTO generateDashboard() {
        DashboardDTO dashboard = new DashboardDTO();
        LocalDate today = LocalDate.now();
        LocalDate startOfYear = today.withDayOfYear(1);

        List<Account> assetAccounts = accountRepository.findActiveByType(AccountType.ASSET);
        BigDecimal totalAssets = BigDecimal.ZERO;
        for (Account account : assetAccounts) {
            totalAssets = totalAssets.add(accountService.getBalance(account.getId()));
        }
        dashboard.setTotalAssets(totalAssets);

        List<Account> liabilityAccounts = accountRepository.findActiveByType(AccountType.LIABILITY);
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        for (Account account : liabilityAccounts) {
            totalLiabilities = totalLiabilities.add(accountService.getBalance(account.getId()));
        }
        dashboard.setTotalLiabilities(totalLiabilities);

        List<Account> equityAccounts = accountRepository.findActiveByType(AccountType.EQUITY);
        BigDecimal totalEquity = BigDecimal.ZERO;
        for (Account account : equityAccounts) {
            totalEquity = totalEquity.add(accountService.getBalance(account.getId()));
        }
        dashboard.setTotalEquity(totalEquity);

        ProfitLossDTO profitLoss = generateProfitLoss(startOfYear, today);
        dashboard.setTotalRevenue(profitLoss.getTotalRevenue());
        dashboard.setTotalExpenses(profitLoss.getTotalExpenses());
        dashboard.setNetIncome(profitLoss.getNetIncome());

        accountRepository.findByCode("1000").ifPresent(cashAccount -> {
            dashboard.setCashBalance(accountService.getBalance(cashAccount.getId()));
        });

        accountRepository.findByCode("1200").ifPresent(arAccount -> {
            dashboard.setAccountsReceivable(accountService.getBalance(arAccount.getId()));
        });

        accountRepository.findByCode("2000").ifPresent(apAccount -> {
            dashboard.setAccountsPayable(accountService.getBalance(apAccount.getId()));
        });

        dashboard.setPendingJournalEntries(journalEntryRepository.countByStatus(EntryStatus.DRAFT));

        dashboard.setOverdueInvoices((long) invoiceRepository.findOverdueInvoices(today).size());
        dashboard.setOverdueAmount(invoiceRepository.sumTotalByStatus(InvoiceStatus.OVERDUE));

        return dashboard;
    }
}