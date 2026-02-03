package com.accounting.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Report Data Transfer Objects
 */
public class ReportDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TrialBalanceDTO {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate asOfDate;
        private List<TrialBalanceLineDTO> lines;
        private BigDecimal totalDebit;
        private BigDecimal totalCredit;
        private boolean balanced;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrialBalanceLineDTO {
        private String accountCode;
        private String accountName;
        private String accountType;
        private BigDecimal debitBalance;
        private BigDecimal creditBalance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProfitLossDTO {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
        private List<AccountBalanceDTO> revenueAccounts;
        private List<AccountBalanceDTO> expenseAccounts;
        private BigDecimal totalRevenue;
        private BigDecimal totalExpenses;
        private BigDecimal netIncome;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BalanceSheetDTO {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate asOfDate;
        private List<AccountBalanceDTO> assetAccounts;
        private List<AccountBalanceDTO> liabilityAccounts;
        private List<AccountBalanceDTO> equityAccounts;
        private BigDecimal totalAssets;
        private BigDecimal totalLiabilities;
        private BigDecimal totalEquity;
        private boolean balanced;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountBalanceDTO {
        private String accountCode;
        private String accountName;
        private BigDecimal balance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LedgerDTO {
        private Long accountId;
        private String accountCode;
        private String accountName;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
        private BigDecimal openingBalance;
        private BigDecimal closingBalance;
        private List<LedgerEntryDTO> entries;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LedgerEntryDTO {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private String reference;
        private String description;
        private BigDecimal debit;
        private BigDecimal credit;
        private BigDecimal balance;
    }
}