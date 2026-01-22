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
public class BalanceSheetDTO {
    private LocalDate asOfDate;
    private List<AccountBalanceDTO> assetAccounts = new ArrayList<>();
    private List<AccountBalanceDTO> liabilityAccounts = new ArrayList<>();
    private List<AccountBalanceDTO> equityAccounts = new ArrayList<>();
    private BigDecimal totalAssets = BigDecimal.ZERO;
    private BigDecimal totalLiabilities = BigDecimal.ZERO;
    private BigDecimal totalEquity = BigDecimal.ZERO;
    private BigDecimal retainedEarnings = BigDecimal.ZERO;

    public void addAssetAccount(AccountBalanceDTO account) {
        assetAccounts.add(account);
        totalAssets = totalAssets.add(account.getBalance());
    }

    public void addLiabilityAccount(AccountBalanceDTO account) {
        liabilityAccounts.add(account);
        totalLiabilities = totalLiabilities.add(account.getBalance());
    }

    public void addEquityAccount(AccountBalanceDTO account) {
        equityAccounts.add(account);
        totalEquity = totalEquity.add(account.getBalance());
    }

    public void setRetainedEarnings(BigDecimal retainedEarnings) {
        this.retainedEarnings = retainedEarnings;
    }

    public BigDecimal getTotalEquityWithRetainedEarnings() {
        return totalEquity.add(retainedEarnings);
    }

    public BigDecimal getTotalLiabilitiesAndEquity() {
        return totalLiabilities.add(getTotalEquityWithRetainedEarnings());
    }

    public boolean isBalanced() {
        return totalAssets.compareTo(getTotalLiabilitiesAndEquity()) == 0;
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