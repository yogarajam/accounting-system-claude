package com.accounting.controller;

import com.accounting.model.Customer;
import com.accounting.model.Invoice;
import com.accounting.model.InvoiceItem;
import com.accounting.model.InvoiceStatus;
import com.accounting.service.AccountService;
import com.accounting.service.CurrencyService;
import com.accounting.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final AccountService accountService;
    private final CurrencyService currencyService;

    @GetMapping
    public String listInvoices(@RequestParam(required = false) String status, Model model) {
        List<Invoice> invoices;
        if (status != null && !status.isEmpty()) {
            invoices = invoiceService.findByStatus(InvoiceStatus.valueOf(status));
        } else {
            invoices = invoiceService.findAll();
        }

        model.addAttribute("invoices", invoices);
        model.addAttribute("statuses", InvoiceStatus.values());
        model.addAttribute("selectedStatus", status);

        return "invoices/list";
    }

    @GetMapping("/new")
    public String newInvoiceForm(Model model) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));

        model.addAttribute("invoice", invoice);
        model.addAttribute("customers", invoiceService.findAllCustomers());
        model.addAttribute("currencies", currencyService.findAll());
        model.addAttribute("revenueAccounts", accountService.findActiveByType(
                com.accounting.model.AccountType.REVENUE));

        return "invoices/form";
    }

    @PostMapping("/save")
    public String saveInvoice(@ModelAttribute Invoice invoice, RedirectAttributes redirectAttributes) {
        if (invoice.getId() == null) {
            invoiceService.createInvoice(invoice);
            redirectAttributes.addFlashAttribute("successMessage", "Invoice created successfully");
        } else {
            invoiceService.updateInvoice(invoice.getId(), invoice);
            redirectAttributes.addFlashAttribute("successMessage", "Invoice updated successfully");
        }
        return "redirect:/invoices";
    }

    @GetMapping("/edit/{id}")
    public String editInvoiceForm(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));

        model.addAttribute("invoice", invoice);
        model.addAttribute("customers", invoiceService.findAllCustomers());
        model.addAttribute("currencies", currencyService.findAll());
        model.addAttribute("revenueAccounts", accountService.findActiveByType(
                com.accounting.model.AccountType.REVENUE));

        return "invoices/form";
    }

    @GetMapping("/view/{id}")
    public String viewInvoice(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.findByIdWithItems(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));

        model.addAttribute("invoice", invoice);
        return "invoices/view";
    }

    @PostMapping("/send/{id}")
    public String sendInvoice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        invoiceService.sendInvoice(id);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice sent and journal entry created");
        return "redirect:/invoices";
    }

    @PostMapping("/pay/{id}")
    public String markAsPaid(@PathVariable Long id,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate,
                             RedirectAttributes redirectAttributes) {
        invoiceService.markAsPaid(id, paymentDate);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice marked as paid");
        return "redirect:/invoices";
    }

    @PostMapping("/cancel/{id}")
    public String cancelInvoice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        invoiceService.cancelInvoice(id);
        redirectAttributes.addFlashAttribute("successMessage", "Invoice cancelled");
        return "redirect:/invoices";
    }

    @GetMapping("/customers")
    public String listCustomers(Model model) {
        model.addAttribute("customers", invoiceService.findAllCustomers());
        return "invoices/customers";
    }

    @GetMapping("/customers/new")
    public String newCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("arAccounts", accountService.findActiveByType(
                com.accounting.model.AccountType.ASSET));
        return "invoices/customer-form";
    }

    @PostMapping("/customers/save")
    public String saveCustomer(@ModelAttribute Customer customer, RedirectAttributes redirectAttributes) {
        invoiceService.saveCustomer(customer);
        redirectAttributes.addFlashAttribute("successMessage", "Customer saved successfully");
        return "redirect:/invoices/customers";
    }

    @GetMapping("/customers/edit/{id}")
    public String editCustomerForm(@PathVariable Long id, Model model) {
        Customer customer = invoiceService.findCustomerById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));

        model.addAttribute("customer", customer);
        model.addAttribute("arAccounts", accountService.findActiveByType(
                com.accounting.model.AccountType.ASSET));
        return "invoices/customer-form";
    }
}