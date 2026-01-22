package com.accounting.service;

import com.accounting.dto.JournalEntryDTO;
import com.accounting.exception.AccountingException;
import com.accounting.model.*;
import com.accounting.repository.AccountRepository;
import com.accounting.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;

    public List<JournalEntry> findAll() {
        return journalEntryRepository.findAll();
    }

    public Page<JournalEntry> findAll(Pageable pageable) {
        return journalEntryRepository.findAll(pageable);
    }

    public Optional<JournalEntry> findById(Long id) {
        return journalEntryRepository.findById(id);
    }

    public Optional<JournalEntry> findByIdWithLines(Long id) {
        return journalEntryRepository.findByIdWithLines(id);
    }

    public List<JournalEntry> findByStatus(EntryStatus status) {
        return journalEntryRepository.findByStatus(status);
    }

    public List<JournalEntry> findPostedEntriesBetweenDates(LocalDate startDate, LocalDate endDate) {
        return journalEntryRepository.findPostedEntriesBetweenDates(startDate, endDate);
    }

    public Long countByStatus(EntryStatus status) {
        return journalEntryRepository.countByStatus(status);
    }

    @Transactional
    public JournalEntry createEntry(JournalEntryDTO dto, User createdBy) {
        JournalEntry entry = new JournalEntry();
        entry.setEntryNumber(generateEntryNumber());
        entry.setEntryDate(dto.getEntryDate());
        entry.setDescription(dto.getDescription());
        entry.setReference(dto.getReference());
        entry.setStatus(EntryStatus.DRAFT);
        entry.setCreatedBy(createdBy);

        for (JournalEntryDTO.JournalEntryLineDTO lineDto : dto.getLines()) {
            Account account = accountRepository.findById(lineDto.getAccountId())
                    .orElseThrow(() -> new AccountingException("Account not found: " + lineDto.getAccountId()));

            JournalEntryLine line = new JournalEntryLine();
            line.setAccount(account);
            line.setDebitAmount(lineDto.getDebitAmount() != null ? lineDto.getDebitAmount() : BigDecimal.ZERO);
            line.setCreditAmount(lineDto.getCreditAmount() != null ? lineDto.getCreditAmount() : BigDecimal.ZERO);
            line.setDescription(lineDto.getDescription());
            entry.addLine(line);
        }

        validateEntry(entry);
        return journalEntryRepository.save(entry);
    }

    @Transactional
    public JournalEntry updateEntry(Long id, JournalEntryDTO dto) {
        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new AccountingException("Journal entry not found: " + id));

        if (!entry.isDraft()) {
            throw new AccountingException("Only draft entries can be modified");
        }

        entry.setEntryDate(dto.getEntryDate());
        entry.setDescription(dto.getDescription());
        entry.setReference(dto.getReference());

        entry.getLines().clear();

        for (JournalEntryDTO.JournalEntryLineDTO lineDto : dto.getLines()) {
            Account account = accountRepository.findById(lineDto.getAccountId())
                    .orElseThrow(() -> new AccountingException("Account not found: " + lineDto.getAccountId()));

            JournalEntryLine line = new JournalEntryLine();
            line.setAccount(account);
            line.setDebitAmount(lineDto.getDebitAmount() != null ? lineDto.getDebitAmount() : BigDecimal.ZERO);
            line.setCreditAmount(lineDto.getCreditAmount() != null ? lineDto.getCreditAmount() : BigDecimal.ZERO);
            line.setDescription(lineDto.getDescription());
            entry.addLine(line);
        }

        validateEntry(entry);
        return journalEntryRepository.save(entry);
    }

    @Transactional
    public JournalEntry postEntry(Long id) {
        JournalEntry entry = journalEntryRepository.findByIdWithLines(id)
                .orElseThrow(() -> new AccountingException("Journal entry not found: " + id));

        if (!entry.isDraft()) {
            throw new AccountingException("Only draft entries can be posted");
        }

        validateEntry(entry);

        entry.setStatus(EntryStatus.POSTED);
        entry.setPostedAt(LocalDateTime.now());

        return journalEntryRepository.save(entry);
    }

    @Transactional
    public JournalEntry voidEntry(Long id) {
        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new AccountingException("Journal entry not found: " + id));

        if (entry.isVoid()) {
            throw new AccountingException("Entry is already void");
        }

        if (!entry.isPosted()) {
            throw new AccountingException("Only posted entries can be voided");
        }

        entry.setStatus(EntryStatus.VOID);
        return journalEntryRepository.save(entry);
    }

    @Transactional
    public void deleteEntry(Long id) {
        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new AccountingException("Journal entry not found: " + id));

        if (!entry.isDraft()) {
            throw new AccountingException("Only draft entries can be deleted");
        }

        journalEntryRepository.delete(entry);
    }

    public void validateEntry(JournalEntry entry) {
        if (entry.getLines() == null || entry.getLines().isEmpty()) {
            throw new AccountingException("Journal entry must have at least one line");
        }

        if (entry.getLines().size() < 2) {
            throw new AccountingException("Journal entry must have at least two lines (debit and credit)");
        }

        BigDecimal totalDebit = entry.getTotalDebit();
        BigDecimal totalCredit = entry.getTotalCredit();

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new AccountingException(
                    String.format("Journal entry must balance: Debits (%.2f) ≠ Credits (%.2f)",
                            totalDebit, totalCredit));
        }

        if (totalDebit.compareTo(BigDecimal.ZERO) == 0) {
            throw new AccountingException("Journal entry total cannot be zero");
        }

        for (JournalEntryLine line : entry.getLines()) {
            if (line.getDebitAmount().compareTo(BigDecimal.ZERO) == 0
                && line.getCreditAmount().compareTo(BigDecimal.ZERO) == 0) {
                throw new AccountingException("Each line must have either a debit or credit amount");
            }

            if (line.getDebitAmount().compareTo(BigDecimal.ZERO) > 0
                && line.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                throw new AccountingException("A line cannot have both debit and credit amounts");
            }

            if (!line.getAccount().getIsActive()) {
                throw new AccountingException("Cannot use inactive account: " + line.getAccount().getFullName());
            }
        }
    }

    private String generateEntryNumber() {
        String prefix = "JE-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        Integer maxNumber = journalEntryRepository.findMaxEntryNumberByPrefix(prefix);
        int nextNumber = (maxNumber != null ? maxNumber : 0) + 1;
        return prefix + "-" + String.format("%04d", nextNumber);
    }
}