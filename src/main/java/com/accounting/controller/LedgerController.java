package com.accounting.controller;

import com.accounting.dto.LedgerDTO;
import com.accounting.service.AccountService;
import com.accounting.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;
    private final AccountService accountService;

    @GetMapping
    public String selectAccount(Model model) {
        model.addAttribute("accounts", accountService.findAllActive());
        return "ledger/select";
    }

    @GetMapping("/view/{accountId}")
    public String viewLedger(@PathVariable Long accountId,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                             Model model) {
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfYear(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LedgerDTO ledger = ledgerService.generateLedger(accountId, startDate, endDate);

        model.addAttribute("ledger", ledger);
        model.addAttribute("accounts", accountService.findAllActive());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "ledger/view";
    }
}