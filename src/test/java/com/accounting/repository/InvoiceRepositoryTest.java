package com.accounting.repository;

import com.accounting.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("InvoiceRepository Integration Tests")
class InvoiceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private Customer customer;
    private Invoice draftInvoice;
    private Invoice sentInvoice;
    private Invoice paidInvoice;

    @BeforeEach
    void setUp() {
        // Create customer
        customer = new Customer();
        customer.setCode("CUST001");
        customer.setName("Test Customer");
        customer.setEmail("test@customer.com");
        entityManager.persist(customer);

        // Create invoices
        draftInvoice = createInvoice("INV-202601-0001", InvoiceStatus.DRAFT, LocalDate.now(), BigDecimal.valueOf(1000));
        sentInvoice = createInvoice("INV-202601-0002", InvoiceStatus.SENT, LocalDate.now().minusDays(15), BigDecimal.valueOf(2000));
        sentInvoice.setDueDate(LocalDate.now().minusDays(5)); // Overdue
        paidInvoice = createInvoice("INV-202601-0003", InvoiceStatus.PAID, LocalDate.now().minusDays(30), BigDecimal.valueOf(3000));

        entityManager.persist(draftInvoice);
        entityManager.persist(sentInvoice);
        entityManager.persist(paidInvoice);
        entityManager.flush();
        entityManager.clear();
    }

    private Invoice createInvoice(String invoiceNumber, InvoiceStatus status, LocalDate invoiceDate, BigDecimal amount) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCustomer(customer);
        invoice.setStatus(status);
        invoice.setInvoiceDate(invoiceDate);
        invoice.setDueDate(invoiceDate.plusDays(30));
        invoice.setSubtotal(amount);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(amount);
        return invoice;
    }

    @Nested
    @DisplayName("Find By Invoice Number")
    class FindByInvoiceNumber {

        @Test
        @DisplayName("Should find invoice by invoice number")
        void findByInvoiceNumber_WhenExists_ReturnsInvoice() {
            Optional<Invoice> result = invoiceRepository.findByInvoiceNumber("INV-202601-0001");

            assertThat(result).isPresent();
            assertThat(result.get().getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        }

        @Test
        @DisplayName("Should return empty when invoice number does not exist")
        void findByInvoiceNumber_WhenNotExists_ReturnsEmpty() {
            Optional<Invoice> result = invoiceRepository.findByInvoiceNumber("INV-INVALID");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should check if invoice number exists")
        void existsByInvoiceNumber_ReturnsCorrectResult() {
            assertThat(invoiceRepository.existsByInvoiceNumber("INV-202601-0001")).isTrue();
            assertThat(invoiceRepository.existsByInvoiceNumber("INV-INVALID")).isFalse();
        }
    }

    @Nested
    @DisplayName("Find By Status")
    class FindByStatus {

        @Test
        @DisplayName("Should find invoices by status")
        void findByStatus_ReturnsMatchingInvoices() {
            List<Invoice> result = invoiceRepository.findByStatus(InvoiceStatus.DRAFT);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getInvoiceNumber()).isEqualTo("INV-202601-0001");
        }

        @Test
        @DisplayName("Should return paginated results by status")
        void findByStatus_Pageable_ReturnsPaginatedResults() {
            Page<Invoice> result = invoiceRepository.findByStatus(InvoiceStatus.SENT, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Find By Customer")
    class FindByCustomer {

        @Test
        @DisplayName("Should find invoices by customer ID")
        void findByCustomerId_ReturnsMatchingInvoices() {
            List<Invoice> result = invoiceRepository.findByCustomerId(customer.getId());

            assertThat(result).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Find Overdue Invoices")
    class FindOverdueInvoices {

        @Test
        @DisplayName("Should find overdue invoices")
        void findOverdueInvoices_ReturnsOverdueInvoices() {
            List<Invoice> result = invoiceRepository.findOverdueInvoices(LocalDate.now());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getInvoiceNumber()).isEqualTo("INV-202601-0002");
        }
    }

    @Nested
    @DisplayName("Find By Date Range")
    class FindByDateRange {

        @Test
        @DisplayName("Should find invoices within date range")
        void findByDateRange_ReturnsMatchingInvoices() {
            LocalDate startDate = LocalDate.now().minusDays(35);
            LocalDate endDate = LocalDate.now();

            List<Invoice> result = invoiceRepository.findByDateRange(startDate, endDate);

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Should return empty list when no invoices in date range")
        void findByDateRange_NoMatch_ReturnsEmptyList() {
            LocalDate startDate = LocalDate.now().plusDays(10);
            LocalDate endDate = LocalDate.now().plusDays(20);

            List<Invoice> result = invoiceRepository.findByDateRange(startDate, endDate);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Sum Total By Status")
    class SumTotalByStatus {

        @Test
        @DisplayName("Should sum total amount by status")
        void sumTotalByStatus_ReturnsCorrectSum() {
            BigDecimal result = invoiceRepository.sumTotalByStatus(InvoiceStatus.PAID);

            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(3000));
        }

        @Test
        @DisplayName("Should return zero when no invoices with status")
        void sumTotalByStatus_NoMatch_ReturnsZero() {
            BigDecimal result = invoiceRepository.sumTotalByStatus(InvoiceStatus.CANCELLED);

            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Find Max Invoice Number By Prefix")
    class FindMaxInvoiceNumberByPrefix {

        @Test
        @DisplayName("Should find max invoice number by prefix")
        void findMaxInvoiceNumberByPrefix_ReturnsMaxNumber() {
            Integer result = invoiceRepository.findMaxInvoiceNumberByPrefix("INV-202601");

            assertThat(result).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return null when no invoices with prefix exist")
        void findMaxInvoiceNumberByPrefix_WhenNoMatch_ReturnsNull() {
            Integer result = invoiceRepository.findMaxInvoiceNumberByPrefix("INV-202612");

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Find By ID With Items")
    class FindByIdWithItems {

        @Test
        @DisplayName("Should find invoice by ID with items eagerly loaded")
        void findByIdWithItems_ReturnsInvoiceWithItems() {
            // Add items to invoice
            InvoiceItem item = new InvoiceItem();
            item.setDescription("Test Item");
            item.setQuantity(BigDecimal.valueOf(2));
            item.setUnitPrice(BigDecimal.valueOf(500));
            item.setAmount(BigDecimal.valueOf(1000));
            draftInvoice = invoiceRepository.findById(draftInvoice.getId()).get();
            draftInvoice.addItem(item);
            entityManager.persist(draftInvoice);
            entityManager.flush();
            entityManager.clear();

            Optional<Invoice> result = invoiceRepository.findByIdWithItems(draftInvoice.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getItems()).hasSize(1);
        }
    }
}