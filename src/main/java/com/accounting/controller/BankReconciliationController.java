package com.accounting.controller;

import com.accounting.model.BankAccount;
import com.accounting.model.BankStatement;
import com.accounting.service.AccountService;
import com.accounting.service.BankReconciliationService;
import com.accounting.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/bank")
@RequiredArgsConstructor
public class BankReconciliationController {

    private final BankReconciliationService bankReconciliationService;
    private final AccountService accountService;
    private final CurrencyService currencyService;

    @GetMapping("/accounts")
    public String listBankAccounts(Model model) {
        List<BankAccount> accounts = bankReconciliationService.findAllBankAccounts();
        model.addAttribute("bankAccounts", accounts);
        return "bank/accounts";
    }

    @GetMapping("/accounts/new")
    public String newBankAccountForm(Model model) {
        model.addAttribute("bankAccount", new BankAccount());
        model.addAttribute("glAccounts", accountService.findActiveByType(
                com.accounting.model.AccountType.ASSET));
        model.addAttribute("currencies", currencyService.findAll());
        return "bank/account-form";
    }

    @PostMapping("/accounts/save")
    public String saveBankAccount(@ModelAttribute BankAccount bankAccount, RedirectAttributes redirectAttributes) {
        bankReconciliationService.saveBankAccount(bankAccount);
        redirectAttributes.addFlashAttribute("successMessage", "Bank account saved successfully");
        return "redirect:/bank/accounts";
    }

    @GetMapping("/accounts/edit/{id}")
    public String editBankAccountForm(@PathVariable Long id, Model model) {
        BankAccount bankAccount = bankReconciliationService.findBankAccountById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found: " + id));

        model.addAttribute("bankAccount", bankAccount);
        model.addAttribute("glAccounts", accountService.findActiveByType(
                com.accounting.model.AccountType.ASSET));
        model.addAttribute("currencies", currencyService.findAll());
        return "bank/account-form";
    }

    @GetMapping("/reconciliation")
    public String reconciliationHome(Model model) {
        model.addAttribute("bankAccounts", bankReconciliationService.findActiveBankAccounts());
        return "bank/reconciliation-select";
    }

    @GetMapping("/reconciliation/{bankAccountId}")
    public String reconcile(@PathVariable Long bankAccountId,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                            Model model) {
        BankAccount bankAccount = bankReconciliationService.findBankAccountById(bankAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found: " + bankAccountId));

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<BankStatement> statements = bankReconciliationService
                .findStatementsByDateRange(bankAccountId, startDate, endDate);
        List<BankStatement> unreconciled = bankReconciliationService.findUnreconciledStatements(bankAccountId);

        model.addAttribute("bankAccount", bankAccount);
        model.addAttribute("statements", statements);
        model.addAttribute("unreconciledStatements", unreconciled);
        model.addAttribute("reconciledBalance", bankReconciliationService.getReconciledBalance(bankAccountId));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "bank/reconciliation";
    }

    @PostMapping("/reconciliation/match")
    public String matchStatement(@RequestParam Long statementId,
                                 @RequestParam Long journalLineId,
                                 @RequestParam Long bankAccountId,
                                 RedirectAttributes redirectAttributes) {
        bankReconciliationService.reconcileStatement(statementId, journalLineId);
        redirectAttributes.addFlashAttribute("successMessage", "Statement matched successfully");
        return "redirect:/bank/reconciliation/" + bankAccountId;
    }

    @PostMapping("/reconciliation/unmatch/{statementId}")
    public String unmatchStatement(@PathVariable Long statementId,
                                   @RequestParam Long bankAccountId,
                                   RedirectAttributes redirectAttributes) {
        bankReconciliationService.unreconcileStatement(statementId);
        redirectAttributes.addFlashAttribute("successMessage", "Statement unmatched successfully");
        return "redirect:/bank/reconciliation/" + bankAccountId;
    }

    @GetMapping("/statements/import/{bankAccountId}")
    public String importStatementsForm(@PathVariable Long bankAccountId, Model model) {
        BankAccount bankAccount = bankReconciliationService.findBankAccountById(bankAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found: " + bankAccountId));

        model.addAttribute("bankAccount", bankAccount);
        return "bank/import";
    }

    @PostMapping("/statements/import/{bankAccountId}")
    public String importStatements(@PathVariable Long bankAccountId,
                                   @ModelAttribute BankStatement statement,
                                   RedirectAttributes redirectAttributes) {
        BankAccount bankAccount = bankReconciliationService.findBankAccountById(bankAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found: " + bankAccountId));

        statement.setBankAccount(bankAccount);
        bankReconciliationService.importStatement(statement);

        redirectAttributes.addFlashAttribute("successMessage", "Statement imported successfully");
        return "redirect:/bank/reconciliation/" + bankAccountId;
    }
}