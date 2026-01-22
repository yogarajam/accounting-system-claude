package com.accounting.controller;

import com.accounting.model.Account;
import com.accounting.model.AccountType;
import com.accounting.service.AccountService;
import com.accounting.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final CurrencyService currencyService;

    @GetMapping
    public String listAccounts(Model model) {
        model.addAttribute("accounts", accountService.findAllActive());
        model.addAttribute("accountTypes", AccountType.values());
        return "accounts/list";
    }

    @GetMapping("/all")
    public String listAllAccounts(Model model) {
        model.addAttribute("accounts", accountService.findAll());
        model.addAttribute("accountTypes", AccountType.values());
        model.addAttribute("showInactive", true);
        return "accounts/list";
    }

    @GetMapping("/new")
    public String newAccountForm(Model model) {
        model.addAttribute("account", new Account());
        model.addAttribute("accountTypes", AccountType.values());
        model.addAttribute("currencies", currencyService.findAll());
        model.addAttribute("parentAccounts", accountService.findAllActive());
        return "accounts/form";
    }

    @PostMapping("/save")
    public String saveAccount(@ModelAttribute Account account, RedirectAttributes redirectAttributes) {
        accountService.save(account);
        redirectAttributes.addFlashAttribute("successMessage", "Account saved successfully");
        return "redirect:/accounts";
    }

    @GetMapping("/edit/{id}")
    public String editAccountForm(@PathVariable Long id, Model model) {
        Account account = accountService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        model.addAttribute("account", account);
        model.addAttribute("accountTypes", AccountType.values());
        model.addAttribute("currencies", currencyService.findAll());
        model.addAttribute("parentAccounts", accountService.findAllActive());
        return "accounts/form";
    }

    @GetMapping("/view/{id}")
    public String viewAccount(@PathVariable Long id, Model model) {
        Account account = accountService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        BigDecimal balance = accountService.getBalance(id);
        model.addAttribute("account", account);
        model.addAttribute("balance", balance);
        return "accounts/view";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        accountService.deactivate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Account deactivated successfully");
        return "redirect:/accounts";
    }

    @PostMapping("/activate/{id}")
    public String activateAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        accountService.activate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Account activated successfully");
        return "redirect:/accounts/all";
    }

    @GetMapping("/by-type/{type}")
    public String listByType(@PathVariable String type, Model model) {
        AccountType accountType = AccountType.valueOf(type.toUpperCase());
        model.addAttribute("accounts", accountService.findActiveByType(accountType));
        model.addAttribute("accountTypes", AccountType.values());
        model.addAttribute("selectedType", accountType);
        return "accounts/list";
    }
}