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
import java.util.ArrayList;
import java.util.List;

/**
 * Journal Entry Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JournalEntryDTO {

    private Long id;
    private String entryNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate entryDate;

    private String description;
    private String reference;
    private String status;
    private String createdBy;

    @Builder.Default
    private List<JournalEntryLineDTO> lines = new ArrayList<>();

    public void addLine(JournalEntryLineDTO line) {
        if (lines == null) {
            lines = new ArrayList<>();
        }
        lines.add(line);
    }

    public static JournalEntryDTO createSimpleEntry(String description, String debitAccountCode,
                                                     String creditAccountCode, BigDecimal amount) {
        JournalEntryDTO entry = JournalEntryDTO.builder()
                .entryDate(LocalDate.now())
                .description(description)
                .reference("REF-" + System.currentTimeMillis())
                .build();

        entry.addLine(JournalEntryLineDTO.builder()
                .accountCode(debitAccountCode)
                .debitAmount(amount)
                .creditAmount(BigDecimal.ZERO)
                .build());

        entry.addLine(JournalEntryLineDTO.builder()
                .accountCode(creditAccountCode)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(amount)
                .build());

        return entry;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
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