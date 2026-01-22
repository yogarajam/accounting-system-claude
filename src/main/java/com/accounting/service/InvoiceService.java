package com.accounting.service;

import com.accounting.exception.AccountingException;
import com.accounting.model.*;
import com.accounting.repository.AccountRepository;
import com.accounting.repository.CustomerRepository;
import com.accounting.repository.InvoiceRepository;
import com.accounting.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
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
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;

    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    public Optional<Invoice> findById(Long id) {
        return invoiceRepository.findById(id);
    }

    public Optional<Invoice> findByIdWithItems(Long id) {
        return invoiceRepository.findByIdWithItems(id);
    }

    public List<Invoice> findByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    public List<Invoice> findByCustomerId(Long customerId) {
        return invoiceRepository.findByCustomerId(customerId);
    }

    public List<Invoice> findOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now());
    }

    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.calculateTotals();
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice updateInvoice(Long id, Invoice invoiceDetails) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AccountingException("Invoice not found: " + id));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new AccountingException("Only draft invoices can be modified");
        }

        invoice.setCustomer(invoiceDetails.getCustomer());
        invoice.setInvoiceDate(invoiceDetails.getInvoiceDate());
        invoice.setDueDate(invoiceDetails.getDueDate());
        invoice.setTaxAmount(invoiceDetails.getTaxAmount());
        invoice.setNotes(invoiceDetails.getNotes());

        invoice.getItems().clear();
        for (InvoiceItem item : invoiceDetails.getItems()) {
            invoice.addItem(item);
        }

        invoice.calculateTotals();
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice sendInvoice(Long id) {
        Invoice invoice = invoiceRepository.findByIdWithItems(id)
                .orElseThrow(() -> new AccountingException("Invoice not found: " + id));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new AccountingException("Only draft invoices can be sent");
        }

        JournalEntry journalEntry = createInvoiceJournalEntry(invoice);
        invoice.setJournalEntry(journalEntry);
        invoice.setStatus(InvoiceStatus.SENT);

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice markAsPaid(Long id, LocalDate paymentDate) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AccountingException("Invoice not found: " + id));

        if (invoice.getStatus() != InvoiceStatus.SENT && invoice.getStatus() != InvoiceStatus.OVERDUE) {
            throw new AccountingException("Only sent or overdue invoices can be marked as paid");
        }

        createPaymentJournalEntry(invoice, paymentDate);
        invoice.setStatus(InvoiceStatus.PAID);

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice cancelInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AccountingException("Invoice not found: " + id));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new AccountingException("Paid invoices cannot be cancelled");
        }

        if (invoice.getJournalEntry() != null) {
            invoice.getJournalEntry().setStatus(EntryStatus.VOID);
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public void updateOverdueInvoices() {
        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(LocalDate.now());
        for (Invoice invoice : overdueInvoices) {
            if (invoice.getStatus() == InvoiceStatus.SENT) {
                invoice.setStatus(InvoiceStatus.OVERDUE);
                invoiceRepository.save(invoice);
            }
        }
    }

    private JournalEntry createInvoiceJournalEntry(Invoice invoice) {
        Account arAccount = accountRepository.findByCode("1200")
                .orElseThrow(() -> new AccountingException("Accounts Receivable account not found"));

        Account revenueAccount = accountRepository.findByCode("4000")
                .orElseThrow(() -> new AccountingException("Sales Revenue account not found"));

        JournalEntry entry = new JournalEntry();
        entry.setEntryNumber(generateJournalEntryNumber());
        entry.setEntryDate(invoice.getInvoiceDate());
        entry.setDescription("Invoice " + invoice.getInvoiceNumber() + " - " + invoice.getCustomer().getName());
        entry.setReference(invoice.getInvoiceNumber());
        entry.setStatus(EntryStatus.POSTED);
        entry.setPostedAt(LocalDateTime.now());

        JournalEntryLine debitLine = new JournalEntryLine();
        debitLine.setAccount(arAccount);
        debitLine.setDebitAmount(invoice.getTotalAmount());
        debitLine.setCreditAmount(BigDecimal.ZERO);
        debitLine.setDescription("Invoice to " + invoice.getCustomer().getName());
        entry.addLine(debitLine);

        JournalEntryLine creditLine = new JournalEntryLine();
        creditLine.setAccount(revenueAccount);
        creditLine.setDebitAmount(BigDecimal.ZERO);
        creditLine.setCreditAmount(invoice.getTotalAmount());
        creditLine.setDescription("Sales revenue");
        entry.addLine(creditLine);

        return journalEntryRepository.save(entry);
    }

    private void createPaymentJournalEntry(Invoice invoice, LocalDate paymentDate) {
        Account cashAccount = accountRepository.findByCode("1000")
                .orElseThrow(() -> new AccountingException("Cash account not found"));

        Account arAccount = accountRepository.findByCode("1200")
                .orElseThrow(() -> new AccountingException("Accounts Receivable account not found"));

        JournalEntry entry = new JournalEntry();
        entry.setEntryNumber(generateJournalEntryNumber());
        entry.setEntryDate(paymentDate);
        entry.setDescription("Payment received for Invoice " + invoice.getInvoiceNumber());
        entry.setReference("PMT-" + invoice.getInvoiceNumber());
        entry.setStatus(EntryStatus.POSTED);
        entry.setPostedAt(LocalDateTime.now());

        JournalEntryLine debitLine = new JournalEntryLine();
        debitLine.setAccount(cashAccount);
        debitLine.setDebitAmount(invoice.getTotalAmount());
        debitLine.setCreditAmount(BigDecimal.ZERO);
        debitLine.setDescription("Payment from " + invoice.getCustomer().getName());
        entry.addLine(debitLine);

        JournalEntryLine creditLine = new JournalEntryLine();
        creditLine.setAccount(arAccount);
        creditLine.setDebitAmount(BigDecimal.ZERO);
        creditLine.setCreditAmount(invoice.getTotalAmount());
        creditLine.setDescription("Clear AR for Invoice " + invoice.getInvoiceNumber());
        entry.addLine(creditLine);

        journalEntryRepository.save(entry);
    }

    private String generateInvoiceNumber() {
        String prefix = "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        Integer maxNumber = invoiceRepository.findMaxInvoiceNumberByPrefix(prefix);
        int nextNumber = (maxNumber != null ? maxNumber : 0) + 1;
        return prefix + "-" + String.format("%04d", nextNumber);
    }

    private String generateJournalEntryNumber() {
        String prefix = "JE-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        Integer maxNumber = journalEntryRepository.findMaxEntryNumberByPrefix(prefix);
        int nextNumber = (maxNumber != null ? maxNumber : 0) + 1;
        return prefix + "-" + String.format("%04d", nextNumber);
    }

    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> findCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    @Transactional
    public Customer saveCustomer(Customer customer) {
        if (customer.getId() == null && customerRepository.existsByCode(customer.getCode())) {
            throw new AccountingException("Customer code already exists: " + customer.getCode());
        }
        return customerRepository.save(customer);
    }
}