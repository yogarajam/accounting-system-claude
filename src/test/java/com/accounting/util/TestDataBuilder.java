package com.accounting.util;

import com.accounting.dto.JournalEntryDTO;
import com.accounting.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestDataBuilder {

    public static Account createAccount(Long id, String code, String name, AccountType type) {
        Account account = new Account();
        account.setId(id);
        account.setCode(code);
        account.setName(name);
        account.setAccountType(type);
        account.setIsActive(true);
        account.setCreatedAt(LocalDateTime.now());
        return account;
    }

    public static Account createCashAccount() {
        return createAccount(1L, "1000", "Cash", AccountType.ASSET);
    }

    public static Account createAccountsReceivable() {
        return createAccount(2L, "1200", "Accounts Receivable", AccountType.ASSET);
    }

    public static Account createAccountsPayable() {
        return createAccount(3L, "2000", "Accounts Payable", AccountType.LIABILITY);
    }

    public static Account createSalesRevenue() {
        return createAccount(4L, "4000", "Sales Revenue", AccountType.REVENUE);
    }

    public static Account createExpenseAccount() {
        return createAccount(5L, "5000", "Operating Expenses", AccountType.EXPENSE);
    }

    public static Account createEquityAccount() {
        return createAccount(6L, "3000", "Owner's Equity", AccountType.EQUITY);
    }

    public static JournalEntry createJournalEntry(Long id, String entryNumber, EntryStatus status) {
        JournalEntry entry = new JournalEntry();
        entry.setId(id);
        entry.setEntryNumber(entryNumber);
        entry.setEntryDate(LocalDate.now());
        entry.setDescription("Test journal entry");
        entry.setStatus(status);
        entry.setCreatedAt(LocalDateTime.now());
        return entry;
    }

    public static JournalEntry createDraftEntry() {
        return createJournalEntry(1L, "JE-202601-0001", EntryStatus.DRAFT);
    }

    public static JournalEntry createPostedEntry() {
        JournalEntry entry = createJournalEntry(2L, "JE-202601-0002", EntryStatus.POSTED);
        entry.setPostedAt(LocalDateTime.now());
        return entry;
    }

    public static JournalEntryLine createJournalEntryLine(Long id, Account account,
            BigDecimal debit, BigDecimal credit) {
        JournalEntryLine line = new JournalEntryLine();
        line.setId(id);
        line.setAccount(account);
        line.setDebitAmount(debit);
        line.setCreditAmount(credit);
        line.setDescription("Test line");
        return line;
    }

    public static JournalEntry createBalancedJournalEntry(Account debitAccount, Account creditAccount,
            BigDecimal amount) {
        JournalEntry entry = createDraftEntry();
        entry.setLines(new ArrayList<>());

        JournalEntryLine debitLine = createJournalEntryLine(1L, debitAccount, amount, BigDecimal.ZERO);
        debitLine.setJournalEntry(entry);
        entry.getLines().add(debitLine);

        JournalEntryLine creditLine = createJournalEntryLine(2L, creditAccount, BigDecimal.ZERO, amount);
        creditLine.setJournalEntry(entry);
        entry.getLines().add(creditLine);

        return entry;
    }

    public static JournalEntryDTO createJournalEntryDTO(Long debitAccountId, Long creditAccountId,
            BigDecimal amount) {
        JournalEntryDTO dto = new JournalEntryDTO();
        dto.setEntryDate(LocalDate.now());
        dto.setDescription("Test journal entry DTO");
        dto.setReference("REF-001");
        dto.setLines(new ArrayList<>());

        JournalEntryDTO.JournalEntryLineDTO debitLine = new JournalEntryDTO.JournalEntryLineDTO();
        debitLine.setAccountId(debitAccountId);
        debitLine.setDebitAmount(amount);
        debitLine.setCreditAmount(BigDecimal.ZERO);
        debitLine.setDescription("Debit line");
        dto.getLines().add(debitLine);

        JournalEntryDTO.JournalEntryLineDTO creditLine = new JournalEntryDTO.JournalEntryLineDTO();
        creditLine.setAccountId(creditAccountId);
        creditLine.setDebitAmount(BigDecimal.ZERO);
        creditLine.setCreditAmount(amount);
        creditLine.setDescription("Credit line");
        dto.getLines().add(creditLine);

        return dto;
    }

    public static Customer createCustomer(Long id, String code, String name) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setCode(code);
        customer.setName(name);
        customer.setEmail(code.toLowerCase() + "@example.com");
        customer.setPhone("123-456-7890");
        customer.setCreatedAt(LocalDateTime.now());
        return customer;
    }

    public static Customer createDefaultCustomer() {
        return createCustomer(1L, "CUST001", "Test Customer");
    }

    // Convenience method - alias for createDefaultCustomer
    public static Customer createCustomer() {
        return createDefaultCustomer();
    }

    // Simplified invoice creation for tests
    public static Invoice createInvoice(Long id, String invoiceNumber, InvoiceStatus status) {
        return createInvoice(id, invoiceNumber, createDefaultCustomer(), status, new BigDecimal("1000.00"));
    }

    public static Invoice createInvoice(Long id, String invoiceNumber, Customer customer,
            InvoiceStatus status, BigDecimal amount) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCustomer(customer);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setStatus(status);
        invoice.setSubtotal(amount);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(amount);
        invoice.setCreatedAt(LocalDateTime.now());
        return invoice;
    }

    public static Invoice createDraftInvoice() {
        return createInvoice(1L, "INV-202601-0001", createDefaultCustomer(),
                InvoiceStatus.DRAFT, new BigDecimal("1000.00"));
    }

    public static Invoice createSentInvoice() {
        return createInvoice(2L, "INV-202601-0002", createDefaultCustomer(),
                InvoiceStatus.SENT, new BigDecimal("1500.00"));
    }

    public static Invoice createOverdueInvoice() {
        Invoice invoice = createInvoice(3L, "INV-202601-0003", createDefaultCustomer(),
                InvoiceStatus.OVERDUE, new BigDecimal("2000.00"));
        invoice.setDueDate(LocalDate.now().minusDays(10));
        return invoice;
    }

    public static InvoiceItem createInvoiceItem(Long id, String description, BigDecimal quantity,
            BigDecimal unitPrice) {
        InvoiceItem item = new InvoiceItem();
        item.setId(id);
        item.setDescription(description);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setAmount(unitPrice.multiply(quantity));
        return item;
    }

    public static User createUser(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("encodedPassword");
        user.setEmail(email);
        user.setFullName("Test User");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    public static User createDefaultUser() {
        return createUser(1L, "testuser", "testuser@example.com");
    }

    public static Role createRole(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }

    public static Role createAdminRole() {
        return createRole(1L, "ADMIN");
    }

    public static Role createUserRole() {
        return createRole(2L, "USER");
    }

    public static BankAccount createBankAccount(Long id, String name, Account glAccount) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setId(id);
        bankAccount.setAccountName(name);
        bankAccount.setAccountNumber("1234567890");
        bankAccount.setBankName("Test Bank");
        bankAccount.setGlAccount(glAccount);
        bankAccount.setOpeningBalance(new BigDecimal("10000.00"));
        bankAccount.setCurrentBalance(new BigDecimal("10000.00"));
        bankAccount.setIsActive(true);
        return bankAccount;
    }

    // Simplified bank statement creation (defaults to credit)
    public static BankStatement createBankStatement(Long id, BankAccount bankAccount, BigDecimal amount) {
        return createBankStatement(id, bankAccount, amount, false);
    }

    public static BankStatement createBankStatement(Long id, BankAccount bankAccount,
            BigDecimal amount, boolean isDebit) {
        BankStatement statement = new BankStatement();
        statement.setId(id);
        statement.setBankAccount(bankAccount);
        statement.setTransactionDate(LocalDate.now());
        statement.setDescription("Test transaction");
        statement.setDebitAmount(isDebit ? amount : BigDecimal.ZERO);
        statement.setCreditAmount(isDebit ? BigDecimal.ZERO : amount);
        statement.setIsReconciled(false);
        statement.setStatementDate(LocalDate.now());
        return statement;
    }

    public static Currency createCurrency(Long id, String code, String name, boolean isBase) {
        Currency currency = new Currency();
        currency.setId(id);
        currency.setCode(code);
        currency.setName(name);
        currency.setSymbol(code.equals("USD") ? "$" : code);
        currency.setExchangeRate(isBase ? BigDecimal.ONE : new BigDecimal("1.10"));
        currency.setIsBase(isBase);
        return currency;
    }

    public static Currency createBaseCurrency() {
        return createCurrency(1L, "USD", "US Dollar", true);
    }

    public static Currency createForeignCurrency() {
        return createCurrency(2L, "EUR", "Euro", false);
    }
}