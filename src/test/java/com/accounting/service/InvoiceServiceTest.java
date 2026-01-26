package com.accounting.service;

import com.accounting.exception.AccountingException;
import com.accounting.model.*;
import com.accounting.repository.AccountRepository;
import com.accounting.repository.CustomerRepository;
import com.accounting.repository.InvoiceRepository;
import com.accounting.repository.JournalEntryRepository;
import com.accounting.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceService Unit Tests")
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private Customer testCustomer;
    private Invoice draftInvoice;
    private Invoice sentInvoice;
    private Invoice overdueInvoice;
    private Account cashAccount;
    private Account arAccount;
    private Account revenueAccount;

    @BeforeEach
    void setUp() {
        testCustomer = TestDataBuilder.createDefaultCustomer();
        draftInvoice = TestDataBuilder.createDraftInvoice();
        sentInvoice = TestDataBuilder.createSentInvoice();
        overdueInvoice = TestDataBuilder.createOverdueInvoice();
        cashAccount = TestDataBuilder.createCashAccount();
        arAccount = TestDataBuilder.createAccountsReceivable();
        revenueAccount = TestDataBuilder.createSalesRevenue();
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @DisplayName("Should find all invoices")
        void findAll_ReturnsAllInvoices() {
            List<Invoice> invoices = Arrays.asList(draftInvoice, sentInvoice);
            when(invoiceRepository.findAll()).thenReturn(invoices);

            List<Invoice> result = invoiceService.findAll();

            assertThat(result).hasSize(2);
            verify(invoiceRepository).findAll();
        }

        @Test
        @DisplayName("Should find invoice by ID")
        void findById_WhenExists_ReturnsInvoice() {
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(draftInvoice));

            Optional<Invoice> result = invoiceService.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getInvoiceNumber()).isEqualTo("INV-202601-0001");
        }

        @Test
        @DisplayName("Should find invoice by ID with items")
        void findByIdWithItems_WhenExists_ReturnsInvoiceWithItems() {
            InvoiceItem item = TestDataBuilder.createInvoiceItem(1L, "Service", BigDecimal.valueOf(2), BigDecimal.valueOf(500));
            draftInvoice.setItems(new ArrayList<>());
            draftInvoice.addItem(item);
            when(invoiceRepository.findByIdWithItems(1L)).thenReturn(Optional.of(draftInvoice));

            Optional<Invoice> result = invoiceService.findByIdWithItems(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getItems()).hasSize(1);
        }

        @Test
        @DisplayName("Should find invoices by status")
        void findByStatus_ReturnsMatchingInvoices() {
            when(invoiceRepository.findByStatus(InvoiceStatus.DRAFT))
                    .thenReturn(Arrays.asList(draftInvoice));

            List<Invoice> result = invoiceService.findByStatus(InvoiceStatus.DRAFT);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        }

        @Test
        @DisplayName("Should find invoices by customer ID")
        void findByCustomerId_ReturnsMatchingInvoices() {
            when(invoiceRepository.findByCustomerId(1L))
                    .thenReturn(Arrays.asList(draftInvoice, sentInvoice));

            List<Invoice> result = invoiceService.findByCustomerId(1L);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should find overdue invoices")
        void findOverdueInvoices_ReturnsOverdueInvoices() {
            when(invoiceRepository.findOverdueInvoices(any(LocalDate.class)))
                    .thenReturn(Arrays.asList(overdueInvoice));

            List<Invoice> result = invoiceService.findOverdueInvoices();

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Create Invoice Operations")
    class CreateInvoiceOperations {

        @Test
        @DisplayName("Should create invoice successfully")
        void createInvoice_ValidInvoice_CreatesSuccessfully() {
            Invoice newInvoice = new Invoice();
            newInvoice.setCustomer(testCustomer);
            newInvoice.setInvoiceDate(LocalDate.now());
            newInvoice.setItems(new ArrayList<>());
            InvoiceItem item = TestDataBuilder.createInvoiceItem(null, "Service", BigDecimal.ONE, BigDecimal.valueOf(1000));
            newInvoice.addItem(item);

            when(invoiceRepository.findMaxInvoiceNumberByPrefix(anyString())).thenReturn(null);
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
                Invoice inv = invocation.getArgument(0);
                inv.setId(1L);
                return inv;
            });

            Invoice result = invoiceService.createInvoice(newInvoice);

            assertThat(result.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
            assertThat(result.getInvoiceNumber()).matches("INV-\\d{6}-0001");
            verify(invoiceRepository).save(any(Invoice.class));
        }

        @Test
        @DisplayName("Should calculate totals when creating invoice")
        void createInvoice_CalculatesTotals() {
            Invoice newInvoice = new Invoice();
            newInvoice.setCustomer(testCustomer);
            newInvoice.setInvoiceDate(LocalDate.now());
            newInvoice.setTaxAmount(BigDecimal.valueOf(100));
            newInvoice.setItems(new ArrayList<>());
            InvoiceItem item1 = TestDataBuilder.createInvoiceItem(null, "Service A", BigDecimal.valueOf(2), BigDecimal.valueOf(500));
            InvoiceItem item2 = TestDataBuilder.createInvoiceItem(null, "Service B", BigDecimal.ONE, BigDecimal.valueOf(300));
            newInvoice.addItem(item1);
            newInvoice.addItem(item2);

            when(invoiceRepository.findMaxInvoiceNumberByPrefix(anyString())).thenReturn(null);
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

            Invoice result = invoiceService.createInvoice(newInvoice);

            assertThat(result.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(1300));
            assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1400));
        }
    }

    @Nested
    @DisplayName("Update Invoice Operations")
    class UpdateInvoiceOperations {

        @Test
        @DisplayName("Should update draft invoice successfully")
        void updateInvoice_DraftInvoice_UpdatesSuccessfully() {
            draftInvoice.setItems(new ArrayList<>());
            Invoice updatedDetails = new Invoice();
            updatedDetails.setCustomer(testCustomer);
            updatedDetails.setInvoiceDate(LocalDate.now());
            updatedDetails.setDueDate(LocalDate.now().plusDays(60));
            updatedDetails.setTaxAmount(BigDecimal.valueOf(50));
            updatedDetails.setNotes("Updated notes");
            updatedDetails.setItems(new ArrayList<>());
            InvoiceItem item = TestDataBuilder.createInvoiceItem(null, "New Service", BigDecimal.ONE, BigDecimal.valueOf(2000));
            updatedDetails.addItem(item);

            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(draftInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

            Invoice result = invoiceService.updateInvoice(1L, updatedDetails);

            assertThat(result.getDueDate()).isEqualTo(LocalDate.now().plusDays(60));
            assertThat(result.getNotes()).isEqualTo("Updated notes");
            verify(invoiceRepository).save(any(Invoice.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-draft invoice")
        void updateInvoice_SentInvoice_ThrowsException() {
            Invoice updatedDetails = new Invoice();
            when(invoiceRepository.findById(2L)).thenReturn(Optional.of(sentInvoice));

            assertThatThrownBy(() -> invoiceService.updateInvoice(2L, updatedDetails))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Only draft invoices can be modified");
        }

        @Test
        @DisplayName("Should throw exception when invoice not found")
        void updateInvoice_NotFound_ThrowsException() {
            Invoice updatedDetails = new Invoice();
            when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.updateInvoice(99L, updatedDetails))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Invoice not found");
        }
    }

    @Nested
    @DisplayName("Send Invoice Operations")
    class SendInvoiceOperations {

        @Test
        @DisplayName("Should send draft invoice and create journal entry")
        void sendInvoice_DraftInvoice_SendsAndCreatesJournalEntry() {
            draftInvoice.setItems(new ArrayList<>());

            when(invoiceRepository.findByIdWithItems(1L)).thenReturn(Optional.of(draftInvoice));
            when(accountRepository.findByCode("1200")).thenReturn(Optional.of(arAccount));
            when(accountRepository.findByCode("4000")).thenReturn(Optional.of(revenueAccount));
            when(journalEntryRepository.findMaxEntryNumberByPrefix(anyString())).thenReturn(null);
            when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(i -> {
                JournalEntry entry = i.getArgument(0);
                entry.setId(1L);
                return entry;
            });
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

            Invoice result = invoiceService.sendInvoice(1L);

            assertThat(result.getStatus()).isEqualTo(InvoiceStatus.SENT);
            assertThat(result.getJournalEntry()).isNotNull();
            verify(journalEntryRepository).save(any(JournalEntry.class));
        }

        @Test
        @DisplayName("Should throw exception when sending non-draft invoice")
        void sendInvoice_SentInvoice_ThrowsException() {
            when(invoiceRepository.findByIdWithItems(2L)).thenReturn(Optional.of(sentInvoice));

            assertThatThrownBy(() -> invoiceService.sendInvoice(2L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Only draft invoices can be sent");
        }

        @Test
        @DisplayName("Should throw exception when AR account not found")
        void sendInvoice_ARAccountNotFound_ThrowsException() {
            draftInvoice.setItems(new ArrayList<>());
            when(invoiceRepository.findByIdWithItems(1L)).thenReturn(Optional.of(draftInvoice));
            when(accountRepository.findByCode("1200")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.sendInvoice(1L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Accounts Receivable account not found");
        }

        @Test
        @DisplayName("Should throw exception when Revenue account not found")
        void sendInvoice_RevenueAccountNotFound_ThrowsException() {
            draftInvoice.setItems(new ArrayList<>());
            when(invoiceRepository.findByIdWithItems(1L)).thenReturn(Optional.of(draftInvoice));
            when(accountRepository.findByCode("1200")).thenReturn(Optional.of(arAccount));
            when(accountRepository.findByCode("4000")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.sendInvoice(1L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Sales Revenue account not found");
        }
    }

    @Nested
    @DisplayName("Mark As Paid Operations")
    class MarkAsPaidOperations {

        @Test
        @DisplayName("Should mark sent invoice as paid and create payment journal entry")
        void markAsPaid_SentInvoice_MarksPaidAndCreatesEntry() {
            LocalDate paymentDate = LocalDate.now();

            when(invoiceRepository.findById(2L)).thenReturn(Optional.of(sentInvoice));
            when(accountRepository.findByCode("1000")).thenReturn(Optional.of(cashAccount));
            when(accountRepository.findByCode("1200")).thenReturn(Optional.of(arAccount));
            when(journalEntryRepository.findMaxEntryNumberByPrefix(anyString())).thenReturn(null);
            when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(i -> {
                JournalEntry entry = i.getArgument(0);
                entry.setId(2L);
                return entry;
            });
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

            Invoice result = invoiceService.markAsPaid(2L, paymentDate);

            assertThat(result.getStatus()).isEqualTo(InvoiceStatus.PAID);
            verify(journalEntryRepository).save(any(JournalEntry.class));
        }

        @Test
        @DisplayName("Should mark overdue invoice as paid")
        void markAsPaid_OverdueInvoice_MarksPaid() {
            LocalDate paymentDate = LocalDate.now();

            when(invoiceRepository.findById(3L)).thenReturn(Optional.of(overdueInvoice));
            when(accountRepository.findByCode("1000")).thenReturn(Optional.of(cashAccount));
            when(accountRepository.findByCode("1200")).thenReturn(Optional.of(arAccount));
            when(journalEntryRepository.findMaxEntryNumberByPrefix(anyString())).thenReturn(null);
            when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(i -> i.getArgument(0));
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

            Invoice result = invoiceService.markAsPaid(3L, paymentDate);

            assertThat(result.getStatus()).isEqualTo(InvoiceStatus.PAID);
        }

        @Test
        @DisplayName("Should throw exception when marking draft invoice as paid")
        void markAsPaid_DraftInvoice_ThrowsException() {
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(draftInvoice));

            assertThatThrownBy(() -> invoiceService.markAsPaid(1L, LocalDate.now()))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Only sent or overdue invoices can be marked as paid");
        }

        @Test
        @DisplayName("Should throw exception when cash account not found")
        void markAsPaid_CashAccountNotFound_ThrowsException() {
            when(invoiceRepository.findById(2L)).thenReturn(Optional.of(sentInvoice));
            when(accountRepository.findByCode("1000")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.markAsPaid(2L, LocalDate.now()))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Cash account not found");
        }
    }

    @Nested
    @DisplayName("Cancel Invoice Operations")
    class CancelInvoiceOperations {

        @Test
        @DisplayName("Should cancel draft invoice")
        void cancelInvoice_DraftInvoice_CancelsSuccessfully() {
            when(invoiceRepository.findById(1L)).thenReturn(Optional.of(draftInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

            Invoice result = invoiceService.cancelInvoice(1L);

            assertThat(result.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should cancel sent invoice and void journal entry")
        void cancelInvoice_SentInvoice_CancelsAndVoidsJournalEntry() {
            JournalEntry journalEntry = TestDataBuilder.createPostedEntry();
            sentInvoice.setJournalEntry(journalEntry);

            when(invoiceRepository.findById(2L)).thenReturn(Optional.of(sentInvoice));
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

            Invoice result = invoiceService.cancelInvoice(2L);

            assertThat(result.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
            assertThat(result.getJournalEntry().getStatus()).isEqualTo(EntryStatus.VOID);
        }

        @Test
        @DisplayName("Should throw exception when cancelling paid invoice")
        void cancelInvoice_PaidInvoice_ThrowsException() {
            Invoice paidInvoice = TestDataBuilder.createInvoice(4L, "INV-202601-0004",
                    testCustomer, InvoiceStatus.PAID, BigDecimal.valueOf(1000));
            when(invoiceRepository.findById(4L)).thenReturn(Optional.of(paidInvoice));

            assertThatThrownBy(() -> invoiceService.cancelInvoice(4L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Paid invoices cannot be cancelled");
        }
    }

    @Nested
    @DisplayName("Update Overdue Invoices Operations")
    class UpdateOverdueInvoicesOperations {

        @Test
        @DisplayName("Should update sent invoices to overdue status")
        void updateOverdueInvoices_UpdatesSentToOverdue() {
            Invoice overdueButSent = TestDataBuilder.createSentInvoice();
            overdueButSent.setDueDate(LocalDate.now().minusDays(5));

            when(invoiceRepository.findOverdueInvoices(any(LocalDate.class)))
                    .thenReturn(Arrays.asList(overdueButSent));
            when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

            invoiceService.updateOverdueInvoices();

            verify(invoiceRepository).save(overdueButSent);
            assertThat(overdueButSent.getStatus()).isEqualTo(InvoiceStatus.OVERDUE);
        }

        @Test
        @DisplayName("Should not update already overdue invoices")
        void updateOverdueInvoices_SkipsAlreadyOverdue() {
            when(invoiceRepository.findOverdueInvoices(any(LocalDate.class)))
                    .thenReturn(Arrays.asList(overdueInvoice));

            invoiceService.updateOverdueInvoices();

            verify(invoiceRepository, never()).save(any(Invoice.class));
        }
    }

    @Nested
    @DisplayName("Customer Operations")
    class CustomerOperations {

        @Test
        @DisplayName("Should find all customers")
        void findAllCustomers_ReturnsAllCustomers() {
            List<Customer> customers = Arrays.asList(testCustomer);
            when(customerRepository.findAll()).thenReturn(customers);

            List<Customer> result = invoiceService.findAllCustomers();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should find customer by ID")
        void findCustomerById_ReturnsCustomer() {
            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

            Optional<Customer> result = invoiceService.findCustomerById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Test Customer");
        }

        @Test
        @DisplayName("Should save new customer successfully")
        void saveCustomer_NewCustomer_SavesSuccessfully() {
            Customer newCustomer = TestDataBuilder.createCustomer(null, "CUST002", "New Customer");
            Customer savedCustomer = TestDataBuilder.createCustomer(2L, "CUST002", "New Customer");

            when(customerRepository.existsByCode("CUST002")).thenReturn(false);
            when(customerRepository.save(newCustomer)).thenReturn(savedCustomer);

            Customer result = invoiceService.saveCustomer(newCustomer);

            assertThat(result.getId()).isEqualTo(2L);
            verify(customerRepository).save(newCustomer);
        }

        @Test
        @DisplayName("Should throw exception when saving customer with duplicate code")
        void saveCustomer_DuplicateCode_ThrowsException() {
            Customer newCustomer = TestDataBuilder.createCustomer(null, "CUST001", "Duplicate");
            when(customerRepository.existsByCode("CUST001")).thenReturn(true);

            assertThatThrownBy(() -> invoiceService.saveCustomer(newCustomer))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Customer code already exists");
        }
    }
}