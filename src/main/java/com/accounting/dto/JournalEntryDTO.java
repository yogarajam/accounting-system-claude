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
public class JournalEntryDTO {
    private Long id;
    private String entryNumber;
    private LocalDate entryDate;
    private String description;
    private String reference;
    private List<JournalEntryLineDTO> lines = new ArrayList<>();

    public void addLine(JournalEntryLineDTO line) {
        lines.add(line);
    }

    public BigDecimal getTotalDebit() {
        return lines.stream()
                .map(JournalEntryLineDTO::getDebitAmount)
                .filter(d -> d != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalCredit() {
        return lines.stream()
                .map(JournalEntryLineDTO::getCreditAmount)
                .filter(c -> c != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isBalanced() {
        return getTotalDebit().compareTo(getTotalCredit()) == 0;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JournalEntryLineDTO {
        private Long id;
        private Long accountId;
        private String accountCode;
        private String accountName;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
        private String description;
    }
}