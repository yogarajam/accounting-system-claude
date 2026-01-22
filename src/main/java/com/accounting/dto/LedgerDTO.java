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
public class LedgerDTO {
    private Long accountId;
    private String accountCode;
    private String accountName;
    private String accountType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal openingBalance = BigDecimal.ZERO;
    private BigDecimal closingBalance = BigDecimal.ZERO;
    private List<LedgerEntryDTO> entries = new ArrayList<>();

    public void addEntry(LedgerEntryDTO entry) {
        entries.add(entry);
    }

    public BigDecimal getTotalDebits() {
        return entries.stream()
                .map(LedgerEntryDTO::getDebitAmount)
                .filter(d -> d != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalCredits() {
        return entries.stream()
                .map(LedgerEntryDTO::getCreditAmount)
                .filter(c -> c != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LedgerEntryDTO {
        private Long journalEntryId;
        private LocalDate entryDate;
        private LocalDate date;
        private String entryNumber;
        private String description;
        private String reference;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
        private BigDecimal runningBalance;

        public LocalDate getEntryDate() {
            return entryDate != null ? entryDate : date;
        }
    }
}