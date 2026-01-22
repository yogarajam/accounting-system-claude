package com.accounting.controller;

import com.accounting.dto.JournalEntryDTO;
import com.accounting.model.Account;
import com.accounting.model.EntryStatus;
import com.accounting.model.JournalEntry;
import com.accounting.model.User;
import com.accounting.service.AccountService;
import com.accounting.service.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/journal")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;
    private final AccountService accountService;

    @GetMapping
    public String listEntries(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              @RequestParam(required = false) String status,
                              Model model) {
        Page<JournalEntry> entries = journalService.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "entryDate", "entryNumber")));

        model.addAttribute("entries", entries);
        model.addAttribute("statuses", EntryStatus.values());
        model.addAttribute("selectedStatus", status);
        return "journal/list";
    }

    @GetMapping("/new")
    public String newEntryForm(Model model) {
        JournalEntryDTO dto = new JournalEntryDTO();
        dto.setEntryDate(LocalDate.now());
        dto.addLine(new JournalEntryDTO.JournalEntryLineDTO());
        dto.addLine(new JournalEntryDTO.JournalEntryLineDTO());

        model.addAttribute("journalEntry", dto);
        model.addAttribute("accounts", accountService.findAllActive());
        return "journal/form";
    }

    @PostMapping("/save")
    public String saveEntry(@ModelAttribute JournalEntryDTO dto,
                            @AuthenticationPrincipal User user,
                            RedirectAttributes redirectAttributes) {
        if (dto.getId() == null) {
            journalService.createEntry(dto, user);
            redirectAttributes.addFlashAttribute("successMessage", "Journal entry created successfully");
        } else {
            journalService.updateEntry(dto.getId(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Journal entry updated successfully");
        }
        return "redirect:/journal";
    }

    @GetMapping("/edit/{id}")
    public String editEntryForm(@PathVariable Long id, Model model) {
        JournalEntry entry = journalService.findByIdWithLines(id)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found: " + id));

        JournalEntryDTO dto = new JournalEntryDTO();
        dto.setId(entry.getId());
        dto.setEntryNumber(entry.getEntryNumber());
        dto.setEntryDate(entry.getEntryDate());
        dto.setDescription(entry.getDescription());
        dto.setReference(entry.getReference());

        entry.getLines().forEach(line -> {
            JournalEntryDTO.JournalEntryLineDTO lineDto = new JournalEntryDTO.JournalEntryLineDTO();
            lineDto.setId(line.getId());
            lineDto.setAccountId(line.getAccount().getId());
            lineDto.setAccountCode(line.getAccount().getCode());
            lineDto.setAccountName(line.getAccount().getName());
            lineDto.setDebitAmount(line.getDebitAmount());
            lineDto.setCreditAmount(line.getCreditAmount());
            lineDto.setDescription(line.getDescription());
            dto.addLine(lineDto);
        });

        model.addAttribute("journalEntry", dto);
        model.addAttribute("accounts", accountService.findAllActive());
        return "journal/form";
    }

    @GetMapping("/view/{id}")
    public String viewEntry(@PathVariable Long id, Model model) {
        JournalEntry entry = journalService.findByIdWithLines(id)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found: " + id));

        model.addAttribute("entry", entry);
        return "journal/view";
    }

    @PostMapping("/post/{id}")
    public String postEntry(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        journalService.postEntry(id);
        redirectAttributes.addFlashAttribute("successMessage", "Journal entry posted successfully");
        return "redirect:/journal";
    }

    @PostMapping("/void/{id}")
    public String voidEntry(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        journalService.voidEntry(id);
        redirectAttributes.addFlashAttribute("successMessage", "Journal entry voided successfully");
        return "redirect:/journal";
    }

    @PostMapping("/delete/{id}")
    public String deleteEntry(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        journalService.deleteEntry(id);
        redirectAttributes.addFlashAttribute("successMessage", "Journal entry deleted successfully");
        return "redirect:/journal";
    }

    @GetMapping("/api/accounts")
    @ResponseBody
    public List<Account> getAccounts() {
        return accountService.findAllActive();
    }
}