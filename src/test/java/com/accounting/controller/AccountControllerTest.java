package com.accounting.controller;

import com.accounting.model.Account;
import com.accounting.model.AccountType;
import com.accounting.service.AccountService;
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

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@DisplayName("AccountController Integration Tests")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private CurrencyService currencyService;

    private Account cashAccount;
    private Account revenueAccount;

    @BeforeEach
    void setUp() {
        cashAccount = TestDataBuilder.createCashAccount();
        revenueAccount = TestDataBuilder.createSalesRevenue();
    }

    @Nested
    @DisplayName("List Accounts")
    class ListAccounts {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display accounts list")
        void listAccounts_ReturnsAccountsListView() throws Exception {
            when(accountService.findAllActive()).thenReturn(Arrays.asList(cashAccount, revenueAccount));

            mockMvc.perform(get("/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("accounts/list"))
                    .andExpect(model().attributeExists("accounts"));
        }

        @Test
        @DisplayName("Should return unauthorized when not authenticated")
        void listAccounts_NotAuthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/accounts"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("View Account")
    class ViewAccount {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display account details")
        void viewAccount_WhenExists_ReturnsDetailView() throws Exception {
            when(accountService.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(accountService.getBalance(1L)).thenReturn(java.math.BigDecimal.valueOf(1000));

            mockMvc.perform(get("/accounts/view/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("accounts/view"))
                    .andExpect(model().attributeExists("account"));
        }
    }

    @Nested
    @DisplayName("Create Account")
    class CreateAccount {

        @Test
        @WithMockUser(username = "testuser", roles = {"ADMIN"})
        @DisplayName("Should display create form")
        void showCreateForm_ReturnsCreateView() throws Exception {
            when(accountService.findAllActive()).thenReturn(Arrays.asList(cashAccount));
            when(currencyService.findAll()).thenReturn(java.util.Collections.emptyList());

            mockMvc.perform(get("/accounts/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("accounts/form"))
                    .andExpect(model().attributeExists("account"))
                    .andExpect(model().attributeExists("accountTypes"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"ADMIN"})
        @DisplayName("Should create account and redirect on success")
        void createAccount_ValidInput_RedirectsToList() throws Exception {
            Account newAccount = TestDataBuilder.createAccount(5L, "1100", "Bank Account", AccountType.ASSET);
            when(accountService.save(any(Account.class))).thenReturn(newAccount);

            mockMvc.perform(post("/accounts/save")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("code", "1100")
                            .param("name", "Bank Account")
                            .param("accountType", "ASSET"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/accounts"));

            verify(accountService).save(any(Account.class));
        }
    }

    @Nested
    @DisplayName("Edit Account")
    class EditAccount {

        @Test
        @WithMockUser(username = "testuser", roles = {"ADMIN"})
        @DisplayName("Should display edit form")
        void showEditForm_WhenExists_ReturnsEditView() throws Exception {
            when(accountService.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(accountService.findAllActive()).thenReturn(Arrays.asList(cashAccount));
            when(currencyService.findAll()).thenReturn(java.util.Collections.emptyList());

            mockMvc.perform(get("/accounts/edit/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("accounts/form"))
                    .andExpect(model().attributeExists("account"));
        }
    }

    @Nested
    @DisplayName("Activate/Deactivate Account")
    class ActivateDeactivateAccount {

        @Test
        @WithMockUser(username = "testuser", roles = {"ADMIN"})
        @DisplayName("Should deactivate account and redirect")
        void deactivateAccount_ValidAccount_RedirectsToList() throws Exception {
            doNothing().when(accountService).deactivate(1L);

            mockMvc.perform(post("/accounts/deactivate/1")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/accounts"));

            verify(accountService).deactivate(1L);
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"ADMIN"})
        @DisplayName("Should activate account and redirect")
        void activateAccount_ValidAccount_RedirectsToAll() throws Exception {
            doNothing().when(accountService).activate(1L);

            mockMvc.perform(post("/accounts/activate/1")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/accounts/all"));

            verify(accountService).activate(1L);
        }
    }
}