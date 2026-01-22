package com.accounting.controller;

import com.accounting.dto.BalanceSheetDTO;
import com.accounting.dto.ProfitLossDTO;
import com.accounting.dto.TrialBalanceDTO;
import com.accounting.service.LedgerService;
import com.accounting.service.ReportService;
import com.accounting.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final LedgerService ledgerService;
    private final AccountService accountService;

    @GetMapping
    public String reportsHome() {
        return "reports/index";
    }

    @GetMapping("/trial-balance")
    public String trialBalance(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate,
                               Model model) {
        if (asOfDate == null) {
            asOfDate = LocalDate.now();
        }

        TrialBalanceDTO trialBalance = reportService.generateTrialBalance(asOfDate);

        model.addAttribute("trialBalance", trialBalance);
        model.addAttribute("asOfDate", asOfDate);

        return "reports/trial-balance";
    }

    @GetMapping("/profit-loss")
    public String profitLoss(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                             Model model) {
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfYear(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        ProfitLossDTO profitLoss = reportService.generateProfitLoss(startDate, endDate);

        model.addAttribute("profitLoss", profitLoss);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "reports/profit-loss";
    }

    @GetMapping("/balance-sheet")
    public String balanceSheet(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate,
                               Model model) {
        if (asOfDate == null) {
            asOfDate = LocalDate.now();
        }

        BalanceSheetDTO balanceSheet = reportService.generateBalanceSheet(asOfDate);

        model.addAttribute("balanceSheet", balanceSheet);
        model.addAttribute("asOfDate", asOfDate);

        return "reports/balance-sheet";
    }

    @GetMapping("/general-ledger")
    public String generalLedger(@RequestParam(required = false) Long accountId,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                Model model) {
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfYear(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        model.addAttribute("accounts", accountService.findAllActive());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        if (accountId != null) {
            model.addAttribute("ledger", ledgerService.generateLedger(accountId, startDate, endDate));
            model.addAttribute("selectedAccountId", accountId);
        }

        return "reports/general-ledger";
    }
}