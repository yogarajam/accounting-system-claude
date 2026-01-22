package com.accounting.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfitLossDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<AccountBalanceDTO> revenueAccounts = new ArrayList<>();
    private List<AccountBalanceDTO> expenseAccounts = new ArrayList<>();
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private BigDecimal netIncome = BigDecimal.ZERO;

    public void addRevenueAccount(AccountBalanceDTO account) {
        revenueAccounts.add(account);
        totalRevenue = totalRevenue.add(account.getBalance());
    }

    public void addExpenseAccount(AccountBalanceDTO account) {
        expenseAccounts.add(account);
        totalExpenses = totalExpenses.add(account.getBalance());
    }

    public void calculateNetIncome() {
        this.netIncome = totalRevenue.subtract(totalExpenses);
    }

    public boolean isProfitable() {
        return netIncome.compareTo(BigDecimal.ZERO) > 0;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountBalanceDTO {
        private Long accountId;
        private String accountCode;
        private String accountName;
        private BigDecimal balance;
    }
}