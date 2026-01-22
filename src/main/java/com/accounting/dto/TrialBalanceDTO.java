package com.accounting.dto;

import com.accounting.model.AccountType;
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
public class TrialBalanceDTO {
    private LocalDate asOfDate;
    private List<TrialBalanceLineDTO> lines = new ArrayList<>();
    private BigDecimal totalDebit = BigDecimal.ZERO;
    private BigDecimal totalCredit = BigDecimal.ZERO;

    public void addLine(TrialBalanceLineDTO line) {
        lines.add(line);
        if (line.getDebitBalance() != null) {
            totalDebit = totalDebit.add(line.getDebitBalance());
        }
        if (line.getCreditBalance() != null) {
            totalCredit = totalCredit.add(line.getCreditBalance());
        }
    }

    public boolean isBalanced() {
        return totalDebit.compareTo(totalCredit) == 0;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrialBalanceLineDTO {
        private Long accountId;
        private String accountCode;
        private String accountName;
        private AccountType accountType;
        private BigDecimal debitBalance;
        private BigDecimal creditBalance;

        public TrialBalanceLineDTO(Long accountId, String accountCode, String accountName,
                                   AccountType accountType, BigDecimal balance) {
            this.accountId = accountId;
            this.accountCode = accountCode;
            this.accountName = accountName;
            this.accountType = accountType;

            if (balance.compareTo(BigDecimal.ZERO) >= 0) {
                if (accountType.isDebitNormal()) {
                    this.debitBalance = balance;
                    this.creditBalance = BigDecimal.ZERO;
                } else {
                    this.creditBalance = balance;
                    this.debitBalance = BigDecimal.ZERO;
                }
            } else {
                if (accountType.isDebitNormal()) {
                    this.creditBalance = balance.abs();
                    this.debitBalance = BigDecimal.ZERO;
                } else {
                    this.debitBalance = balance.abs();
                    this.creditBalance = BigDecimal.ZERO;
                }
            }
        }
    }
}