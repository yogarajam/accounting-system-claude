package com.accounting.integration;

import com.accounting.exception.AccountingException;
import com.accounting.model.*;
import com.accounting.repository.*;
import com.accounting.service.AccountService;
import com.accounting.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Invoice End-to-End Integration Tests")
class InvoiceIntegrationTest {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    private Account cashAccount;
    private Account arAccount;
    private Account revenueAccount;
    private Customer customer;

    @BeforeEach
    void setUp() {
        // Create required accounts
        cashAccount = createAccount("1000", "Cash", AccountType.ASSET);
        arAccount = createAccount("1200", "Accounts Receivable", AccountType.ASSET);
        revenueAccount = createAccount("4000", "Sales Revenue", AccountType.REVENUE);

        // Create test customer
        customer = createCustomer();
    }

    private Account createAccount(String code, String name, AccountType type) {
        Account account = new Account();
        account.setCode(code);
        account.setName(name);
        account.setAccountType(type);
        account.setIsActive(true);
        return accountRepository.save(account);
    }

    private Customer createCustomer() {
        Customer c = new Customer();
        c.setCode("CUST001");
        c.setName("Test Customer");
        c.setEmail("test@customer.com");
        c.setPhone("123-456-7890");
        return customerRepository.save(c);
    }

    private Invoice createTestInvoice(BigDecimal amount) {
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setItems(new ArrayList<>());

        InvoiceItem item = new InvoiceItem();
        item.setDescription("Test Service");
        item.setQuantity(BigDecimal.ONE);
        item.setUnitPrice(amount);
        item.setAmount(amount);
        invoice.addItem(item);

        return invoice;
    }

    @Nested
    @DisplayName("Complete Invoice Lifecycle")
    class InvoiceLifecycle {

        @Test
        @DisplayName("Should complete full invoice lifecycle: Create -> Send -> Pay")
        void invoiceLifecycle_CreateSendPay_Succeeds() {
            // 1. CREATE INVOICE
            Invoice invoice = createTestInvoice(BigDecimal.valueOf(1000));
            Invoice createdInvoice = invoiceService.createInvoice(invoice);

            assertThat(createdInvoice.getId()).isNotNull();
            assertThat(createdInvoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
            assertThat(createdInvoice.getInvoiceNumber()).startsWith("INV-");
            assertThat(createdInvoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));

            // 2. SEND INVOICE
            Invoice sentInvoice = invoiceService.sendInvoice(createdInvoice.getId());

            assertThat(sentInvoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
            assertThat(sentInvoice.getJournalEntry()).isNotNull();
            assertThat(sentInvoice.getJournalEntry().getStatus()).isEqualTo(EntryStatus.POSTED);

            // Verify AR balance increased
            BigDecimal arBalance = accountService.getBalance(arAccount.getId());
            assertThat(arBalance).isEqualByComparingTo(BigDecimal.valueOf(1000));

            // Verify revenue increased
            BigDecimal revenueBalance = accountService.getBalance(revenueAccount.getId());
            assertThat(revenueBalance).isEqualByComparingTo(BigDecimal.valueOf(1000));

            // 3. MARK AS PAID
            LocalDate paymentDate = LocalDate.now();
            Invoice paidInvoice = invoiceService.markAsPaid(sentInvoice.getId(), paymentDate);

            assertThat(paidInvoice.getStatus()).isEqualTo(InvoiceStatus.PAID);

            // Verify cash increased and AR decreased
            BigDecimal cashBalance = accountService.getBalance(cashAccount.getId());
            BigDecimal finalArBalance = accountService.getBalance(arAccount.getId());

            assertThat(cashBalance).isEqualByComparingTo(BigDecimal.valueOf(1000));
            assertThat(finalArBalance).isEqualByComparingTo(BigDecimal.ZERO); // AR cleared
        }

        @Test
        @DisplayName("Should cancel sent invoice and void journal entry")
        void cancelSentInvoice_VoidsJournalEntry() {
            Invoice invoice = createTestInvoice(BigDecimal.valueOf(500));
            Invoice createdInvoice = invoiceService.createInvoice(invoice);
            Invoice sentInvoice = invoiceService.sendInvoice(createdInvoice.getId());

            Invoice cancelledInvoice = invoiceService.cancelInvoice(sentInvoice.getId());

            assertThat(cancelledInvoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
            assertThat(cancelledInvoice.getJournalEntry().getStatus()).isEqualTo(EntryStatus.VOID);
        }
    }

    @Nested
    @DisplayName("Invoice Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should reject sending non-draft invoice")
        void sendInvoice_NotDraft_ThrowsException() {
            Invoice invoice = createTestInvoice(BigDecimal.valueOf(1000));
            Invoice createdInvoice = invoiceService.createInvoice(invoice);
            invoiceService.sendInvoice(createdInvoice.getId());

            assertThatThrownBy(() -> invoiceService.sendInvoice(createdInvoice.getId()))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Only draft invoices can be sent");
        }

        @Test
        @DisplayName("Should reject marking draft invoice as paid")
        void markAsPaid_DraftInvoice_ThrowsException() {
            Invoice invoice = createTestInvoice(BigDecimal.valueOf(1000));
            Invoice createdInvoice = invoiceService.createInvoice(invoice);

            assertThatThrownBy(() -> invoiceService.markAsPaid(createdInvoice.getId(), LocalDate.now()))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Only sent or overdue invoices can be marked as paid");
        }

        @Test
        @DisplayName("Should reject cancelling paid invoice")
        void cancelInvoice_PaidInvoice_ThrowsException() {
            Invoice invoice = createTestInvoice(BigDecimal.valueOf(1000));
            Invoice createdInvoice = invoiceService.createInvoice(invoice);
            Invoice sentInvoice = invoiceService.sendInvoice(createdInvoice.getId());
            invoiceService.markAsPaid(sentInvoice.getId(), LocalDate.now());

            assertThatThrownBy(() -> invoiceService.cancelInvoice(sentInvoice.getId()))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Paid invoices cannot be cancelled");
        }

        @Test
        @DisplayName("Should reject updating sent invoice")
        void updateInvoice_SentInvoice_ThrowsException() {
            Invoice invoice = createTestInvoice(BigDecimal.valueOf(1000));
            Invoice createdInvoice = invoiceService.createInvoice(invoice);
            invoiceService.sendInvoice(createdInvoice.getId());

            Invoice updatedDetails = createTestInvoice(BigDecimal.valueOf(2000));

            assertThatThrownBy(() -> invoiceService.updateInvoice(createdInvoice.getId(), updatedDetails))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Only draft invoices can be modified");
        }
    }

    @Nested
    @DisplayName("Invoice Totals Calculation")
    class TotalsCalculation {

        @Test
        @DisplayName("Should calculate invoice totals correctly")
        void createInvoice_CalculatesTotals() {
            Invoice invoice = new Invoice();
            invoice.setCustomer(customer);
            invoice.setInvoiceDate(LocalDate.now());
            invoice.setDueDate(LocalDate.now().plusDays(30));
            invoice.setTaxAmount(BigDecimal.valueOf(100));
            invoice.setItems(new ArrayList<>());

            InvoiceItem item1 = new InvoiceItem();
            item1.setDescription("Service A");
            item1.setQuantity(BigDecimal.valueOf(2));
            item1.setUnitPrice(BigDecimal.valueOf(500));
            item1.setAmount(BigDecimal.valueOf(1000));
            invoice.addItem(item1);

            InvoiceItem item2 = new InvoiceItem();
            item2.setDescription("Service B");
            item2.setQuantity(BigDecimal.ONE);
            item2.setUnitPrice(BigDecimal.valueOf(300));
            item2.setAmount(BigDecimal.valueOf(300));
            invoice.addItem(item2);

            Invoice createdInvoice = invoiceService.createInvoice(invoice);

            assertThat(createdInvoice.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(1300));
            assertThat(createdInvoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1400));
        }
    }

    @Nested
    @DisplayName("Customer Operations")
    class CustomerOperations {

        @Test
        @DisplayName("Should save new customer successfully")
        void saveCustomer_NewCustomer_Succeeds() {
            Customer newCustomer = new Customer();
            newCustomer.setCode("CUST002");
            newCustomer.setName("New Customer");
            newCustomer.setEmail("new@customer.com");

            Customer savedCustomer = invoiceService.saveCustomer(newCustomer);

            assertThat(savedCustomer.getId()).isNotNull();
            assertThat(savedCustomer.getCode()).isEqualTo("CUST002");
        }

        @Test
        @DisplayName("Should reject duplicate customer code")
        void saveCustomer_DuplicateCode_ThrowsException() {
            Customer duplicateCustomer = new Customer();
            duplicateCustomer.setCode("CUST001"); // Same as existing customer
            duplicateCustomer.setName("Duplicate Customer");

            assertThatThrownBy(() -> invoiceService.saveCustomer(duplicateCustomer))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Customer code already exists");
        }
    }
}