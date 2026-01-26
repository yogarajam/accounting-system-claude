package com.accounting.controller;

import com.accounting.model.*;
import com.accounting.service.AccountService;
import com.accounting.service.CurrencyService;
import com.accounting.service.InvoiceService;
import com.accounting.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
@DisplayName("InvoiceController Integration Tests")
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private CurrencyService currencyService;

    private Invoice draftInvoice;
    private Invoice sentInvoice;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = TestDataBuilder.createCustomer();
        draftInvoice = TestDataBuilder.createInvoice(1L, "INV-202601-0001", InvoiceStatus.DRAFT);
        draftInvoice.setCustomer(testCustomer);
        sentInvoice = TestDataBuilder.createInvoice(2L, "INV-202601-0002", InvoiceStatus.SENT);
        sentInvoice.setCustomer(testCustomer);
    }

    @Nested
    @DisplayName("List Invoices")
    class ListInvoices {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display invoices list")
        void listInvoices_ReturnsInvoicesListView() throws Exception {
            when(invoiceService.findAll()).thenReturn(Arrays.asList(draftInvoice, sentInvoice));

            mockMvc.perform(get("/invoices"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("invoices/list"))
                    .andExpect(model().attributeExists("invoices"))
                    .andExpect(model().attributeExists("statuses"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should filter invoices by status")
        void listInvoices_WithStatusFilter_ReturnsFilteredInvoices() throws Exception {
            when(invoiceService.findByStatus(InvoiceStatus.DRAFT))
                    .thenReturn(Collections.singletonList(draftInvoice));

            mockMvc.perform(get("/invoices").param("status", "DRAFT"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("invoices/list"))
                    .andExpect(model().attribute("selectedStatus", "DRAFT"));

            verify(invoiceService).findByStatus(InvoiceStatus.DRAFT);
        }

        @Test
        @DisplayName("Should return unauthorized when not authenticated")
        void listInvoices_NotAuthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/invoices"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("View Invoice")
    class ViewInvoice {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display invoice details")
        void viewInvoice_WhenExists_ReturnsDetailView() throws Exception {
            when(invoiceService.findByIdWithItems(1L)).thenReturn(Optional.of(draftInvoice));

            mockMvc.perform(get("/invoices/view/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("invoices/view"))
                    .andExpect(model().attributeExists("invoice"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should redirect when invoice not found")
        void viewInvoice_WhenNotExists_Redirects() throws Exception {
            when(invoiceService.findByIdWithItems(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/invoices/view/999"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    @DisplayName("Create Invoice")
    class CreateInvoice {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display create form")
        void showCreateForm_ReturnsCreateView() throws Exception {
            when(invoiceService.findAllCustomers()).thenReturn(Collections.singletonList(testCustomer));
            when(currencyService.findAll()).thenReturn(Collections.emptyList());
            when(accountService.findActiveByType(AccountType.REVENUE)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/invoices/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("invoices/form"))
                    .andExpect(model().attributeExists("invoice"))
                    .andExpect(model().attributeExists("customers"))
                    .andExpect(model().attributeExists("currencies"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should create invoice and redirect on success")
        void createInvoice_ValidInput_RedirectsToList() throws Exception {
            when(invoiceService.createInvoice(any(Invoice.class))).thenReturn(draftInvoice);

            mockMvc.perform(post("/invoices/save")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("invoiceDate", "2026-01-23")
                            .param("dueDate", "2026-02-23"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/invoices"));

            verify(invoiceService).createInvoice(any(Invoice.class));
        }
    }

    @Nested
    @DisplayName("Edit Invoice")
    class EditInvoice {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display edit form")
        void showEditForm_WhenExists_ReturnsEditView() throws Exception {
            when(invoiceService.findByIdWithItems(1L)).thenReturn(Optional.of(draftInvoice));
            when(invoiceService.findAllCustomers()).thenReturn(Collections.singletonList(testCustomer));
            when(currencyService.findAll()).thenReturn(Collections.emptyList());
            when(accountService.findActiveByType(AccountType.REVENUE)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/invoices/edit/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("invoices/form"))
                    .andExpect(model().attributeExists("invoice"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should update invoice and redirect on success")
        void updateInvoice_ValidInput_RedirectsToList() throws Exception {
            draftInvoice.setId(1L);
            when(invoiceService.updateInvoice(anyLong(), any(Invoice.class))).thenReturn(draftInvoice);

            mockMvc.perform(post("/invoices/save")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("id", "1")
                            .param("invoiceDate", "2026-01-23")
                            .param("dueDate", "2026-02-23"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/invoices"));

            verify(invoiceService).updateInvoice(anyLong(), any(Invoice.class));
        }
    }

    @Nested
    @DisplayName("Invoice Actions")
    class InvoiceActions {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should send invoice and redirect")
        void sendInvoice_ValidInvoice_RedirectsToList() throws Exception {
            when(invoiceService.sendInvoice(1L)).thenReturn(sentInvoice);

            mockMvc.perform(post("/invoices/send/1")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/invoices"));

            verify(invoiceService).sendInvoice(1L);
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should mark invoice as paid and redirect")
        void markAsPaid_ValidInvoice_RedirectsToList() throws Exception {
            Invoice paidInvoice = TestDataBuilder.createInvoice(1L, "INV-202601-0001", InvoiceStatus.PAID);
            when(invoiceService.markAsPaid(anyLong(), any(LocalDate.class))).thenReturn(paidInvoice);

            mockMvc.perform(post("/invoices/pay/1")
                            .with(csrf())
                            .param("paymentDate", "2026-01-23"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/invoices"));

            verify(invoiceService).markAsPaid(eq(1L), any(LocalDate.class));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should cancel invoice and redirect")
        void cancelInvoice_ValidInvoice_RedirectsToList() throws Exception {
            Invoice cancelledInvoice = TestDataBuilder.createInvoice(1L, "INV-202601-0001", InvoiceStatus.CANCELLED);
            when(invoiceService.cancelInvoice(1L)).thenReturn(cancelledInvoice);

            mockMvc.perform(post("/invoices/cancel/1")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/invoices"));

            verify(invoiceService).cancelInvoice(1L);
        }
    }

    @Nested
    @DisplayName("Customer Operations")
    class CustomerOperations {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display customers list")
        void listCustomers_ReturnsCustomersView() throws Exception {
            when(invoiceService.findAllCustomers()).thenReturn(Collections.singletonList(testCustomer));

            mockMvc.perform(get("/invoices/customers"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("invoices/customers"))
                    .andExpect(model().attributeExists("customers"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display new customer form")
        void newCustomerForm_ReturnsFormView() throws Exception {
            when(accountService.findActiveByType(AccountType.ASSET)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/invoices/customers/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("invoices/customer-form"))
                    .andExpect(model().attributeExists("customer"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should save customer and redirect")
        void saveCustomer_ValidInput_RedirectsToList() throws Exception {
            when(invoiceService.saveCustomer(any(Customer.class))).thenReturn(testCustomer);

            mockMvc.perform(post("/invoices/customers/save")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("code", "CUST001")
                            .param("name", "Test Customer")
                            .param("email", "test@example.com"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/invoices/customers"));

            verify(invoiceService).saveCustomer(any(Customer.class));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display edit customer form")
        void editCustomerForm_WhenExists_ReturnsFormView() throws Exception {
            when(invoiceService.findCustomerById(1L)).thenReturn(Optional.of(testCustomer));
            when(accountService.findActiveByType(AccountType.ASSET)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/invoices/customers/edit/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("invoices/customer-form"))
                    .andExpect(model().attributeExists("customer"));
        }
    }
}