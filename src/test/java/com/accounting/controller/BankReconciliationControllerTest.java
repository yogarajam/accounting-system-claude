package com.accounting.controller;

import com.accounting.model.*;
import com.accounting.service.AccountService;
import com.accounting.service.BankReconciliationService;
import com.accounting.service.CurrencyService;
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

@WebMvcTest(BankReconciliationController.class)
@DisplayName("BankReconciliationController Integration Tests")
class BankReconciliationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankReconciliationService bankReconciliationService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private CurrencyService currencyService;

    private BankAccount bankAccount;
    private BankStatement bankStatement;
    private Account cashAccount;

    @BeforeEach
    void setUp() {
        cashAccount = TestDataBuilder.createCashAccount();
        bankAccount = TestDataBuilder.createBankAccount(1L, "Main Checking", cashAccount);
        bankStatement = TestDataBuilder.createBankStatement(1L, bankAccount, BigDecimal.valueOf(1000));
    }

    @Nested
    @DisplayName("Bank Accounts List")
    class BankAccountsList {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display bank accounts list")
        void listBankAccounts_ReturnsBankAccountsView() throws Exception {
            when(bankReconciliationService.findAllBankAccounts())
                    .thenReturn(Collections.singletonList(bankAccount));

            mockMvc.perform(get("/bank/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bank/accounts"))
                    .andExpect(model().attributeExists("bankAccounts"));
        }

        @Test
        @DisplayName("Should return unauthorized when not authenticated")
        void listBankAccounts_NotAuthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/bank/accounts"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Create Bank Account")
    class CreateBankAccount {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display new bank account form")
        void newBankAccountForm_ReturnsFormView() throws Exception {
            when(accountService.findActiveByType(AccountType.ASSET))
                    .thenReturn(Collections.singletonList(cashAccount));
            when(currencyService.findAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/bank/accounts/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bank/account-form"))
                    .andExpect(model().attributeExists("bankAccount"))
                    .andExpect(model().attributeExists("glAccounts"))
                    .andExpect(model().attributeExists("currencies"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should save bank account and redirect")
        void saveBankAccount_ValidInput_RedirectsToList() throws Exception {
            when(bankReconciliationService.saveBankAccount(any(BankAccount.class)))
                    .thenReturn(bankAccount);

            mockMvc.perform(post("/bank/accounts/save")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("accountName", "Main Checking")
                            .param("accountNumber", "123456789")
                            .param("bankName", "Test Bank"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/bank/accounts"));

            verify(bankReconciliationService).saveBankAccount(any(BankAccount.class));
        }
    }

    @Nested
    @DisplayName("Edit Bank Account")
    class EditBankAccount {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display edit bank account form")
        void editBankAccountForm_WhenExists_ReturnsFormView() throws Exception {
            when(bankReconciliationService.findBankAccountById(1L))
                    .thenReturn(Optional.of(bankAccount));
            when(accountService.findActiveByType(AccountType.ASSET))
                    .thenReturn(Collections.singletonList(cashAccount));
            when(currencyService.findAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/bank/accounts/edit/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bank/account-form"))
                    .andExpect(model().attributeExists("bankAccount"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should redirect when bank account not found")
        void editBankAccountForm_WhenNotExists_Redirects() throws Exception {
            when(bankReconciliationService.findBankAccountById(999L))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/bank/accounts/edit/999"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    @DisplayName("Reconciliation")
    class Reconciliation {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display reconciliation home page")
        void reconciliationHome_ReturnsSelectView() throws Exception {
            when(bankReconciliationService.findActiveBankAccounts())
                    .thenReturn(Collections.singletonList(bankAccount));

            mockMvc.perform(get("/bank/reconciliation"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bank/reconciliation-select"))
                    .andExpect(model().attributeExists("bankAccounts"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display reconciliation page for specific bank account")
        void reconcile_ValidBankAccount_ReturnsReconciliationView() throws Exception {
            when(bankReconciliationService.findBankAccountById(1L))
                    .thenReturn(Optional.of(bankAccount));
            when(bankReconciliationService.findStatementsByDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(Collections.singletonList(bankStatement));
            when(bankReconciliationService.findUnreconciledStatements(1L))
                    .thenReturn(Collections.singletonList(bankStatement));
            when(bankReconciliationService.getReconciledBalance(1L))
                    .thenReturn(BigDecimal.valueOf(5000));

            mockMvc.perform(get("/bank/reconciliation/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bank/reconciliation"))
                    .andExpect(model().attributeExists("bankAccount"))
                    .andExpect(model().attributeExists("statements"))
                    .andExpect(model().attributeExists("unreconciledStatements"))
                    .andExpect(model().attributeExists("reconciledBalance"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display reconciliation page with specified date range")
        void reconcile_WithDateRange_ReturnsFilteredStatements() throws Exception {
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 1, 31);

            when(bankReconciliationService.findBankAccountById(1L))
                    .thenReturn(Optional.of(bankAccount));
            when(bankReconciliationService.findStatementsByDateRange(1L, startDate, endDate))
                    .thenReturn(Collections.singletonList(bankStatement));
            when(bankReconciliationService.findUnreconciledStatements(1L))
                    .thenReturn(Collections.emptyList());
            when(bankReconciliationService.getReconciledBalance(1L))
                    .thenReturn(BigDecimal.valueOf(5000));

            mockMvc.perform(get("/bank/reconciliation/1")
                            .param("startDate", "2026-01-01")
                            .param("endDate", "2026-01-31"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bank/reconciliation"))
                    .andExpect(model().attribute("startDate", startDate))
                    .andExpect(model().attribute("endDate", endDate));

            verify(bankReconciliationService).findStatementsByDateRange(1L, startDate, endDate);
        }
    }

    @Nested
    @DisplayName("Match/Unmatch Statements")
    class MatchUnmatchStatements {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should match statement and redirect")
        void matchStatement_ValidInput_RedirectsToReconciliation() throws Exception {
            doNothing().when(bankReconciliationService).reconcileStatement(1L, 10L);

            mockMvc.perform(post("/bank/reconciliation/match")
                            .with(csrf())
                            .param("statementId", "1")
                            .param("journalLineId", "10")
                            .param("bankAccountId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/bank/reconciliation/1"));

            verify(bankReconciliationService).reconcileStatement(1L, 10L);
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should unmatch statement and redirect")
        void unmatchStatement_ValidInput_RedirectsToReconciliation() throws Exception {
            doNothing().when(bankReconciliationService).unreconcileStatement(1L);

            mockMvc.perform(post("/bank/reconciliation/unmatch/1")
                            .with(csrf())
                            .param("bankAccountId", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/bank/reconciliation/1"));

            verify(bankReconciliationService).unreconcileStatement(1L);
        }
    }

    @Nested
    @DisplayName("Import Statements")
    class ImportStatements {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display import statements form")
        void importStatementsForm_ReturnsImportView() throws Exception {
            when(bankReconciliationService.findBankAccountById(1L))
                    .thenReturn(Optional.of(bankAccount));

            mockMvc.perform(get("/bank/statements/import/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("bank/import"))
                    .andExpect(model().attributeExists("bankAccount"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should import statement and redirect")
        void importStatements_ValidInput_RedirectsToReconciliation() throws Exception {
            when(bankReconciliationService.findBankAccountById(1L))
                    .thenReturn(Optional.of(bankAccount));
            when(bankReconciliationService.importStatement(any(BankStatement.class)))
                    .thenReturn(bankStatement);

            mockMvc.perform(post("/bank/statements/import/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("transactionDate", "2026-01-23")
                            .param("description", "Test Transaction")
                            .param("amount", "1000.00"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/bank/reconciliation/1"));

            verify(bankReconciliationService).importStatement(any(BankStatement.class));
        }
    }
}