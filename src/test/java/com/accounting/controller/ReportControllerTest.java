package com.accounting.controller;

import com.accounting.dto.BalanceSheetDTO;
import com.accounting.dto.LedgerDTO;
import com.accounting.dto.ProfitLossDTO;
import com.accounting.dto.TrialBalanceDTO;
import com.accounting.model.Account;
import com.accounting.model.AccountType;
import com.accounting.service.AccountService;
import com.accounting.service.LedgerService;
import com.accounting.service.ReportService;
import com.accounting.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@DisplayName("ReportController Integration Tests")
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private LedgerService ledgerService;

    @MockBean
    private AccountService accountService;

    private TrialBalanceDTO trialBalanceDTO;
    private ProfitLossDTO profitLossDTO;
    private BalanceSheetDTO balanceSheetDTO;
    private Account cashAccount;

    @BeforeEach
    void setUp() {
        cashAccount = TestDataBuilder.createCashAccount();
        trialBalanceDTO = createTrialBalanceDTO();
        profitLossDTO = createProfitLossDTO();
        balanceSheetDTO = createBalanceSheetDTO();
    }

    private TrialBalanceDTO createTrialBalanceDTO() {
        TrialBalanceDTO dto = new TrialBalanceDTO();
        dto.setAsOfDate(LocalDate.now());
        // Use addLine method to add entries which updates totals
        return dto;
    }

    private ProfitLossDTO createProfitLossDTO() {
        ProfitLossDTO dto = new ProfitLossDTO();
        dto.setStartDate(LocalDate.now().withDayOfYear(1));
        dto.setEndDate(LocalDate.now());
        dto.setTotalRevenue(BigDecimal.valueOf(50000));
        dto.setTotalExpenses(BigDecimal.valueOf(30000));
        dto.setNetIncome(BigDecimal.valueOf(20000));
        return dto;
    }

    private BalanceSheetDTO createBalanceSheetDTO() {
        BalanceSheetDTO dto = new BalanceSheetDTO();
        dto.setAsOfDate(LocalDate.now());
        dto.setTotalAssets(BigDecimal.valueOf(100000));
        dto.setTotalLiabilities(BigDecimal.valueOf(40000));
        dto.setTotalEquity(BigDecimal.valueOf(60000));
        return dto;
    }

    @Nested
    @DisplayName("Reports Home")
    class ReportsHome {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display reports home page")
        void reportsHome_ReturnsIndexView() throws Exception {
            mockMvc.perform(get("/reports"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("reports/index"));
        }

        @Test
        @DisplayName("Should return unauthorized when not authenticated")
        void reportsHome_NotAuthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/reports"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Trial Balance Report")
    class TrialBalanceReport {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display trial balance with default date")
        void trialBalance_DefaultDate_ReturnsReport() throws Exception {
            when(reportService.generateTrialBalance(any(LocalDate.class))).thenReturn(trialBalanceDTO);

            mockMvc.perform(get("/reports/trial-balance"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("reports/trial-balance"))
                    .andExpect(model().attributeExists("trialBalance"))
                    .andExpect(model().attributeExists("asOfDate"));

            verify(reportService).generateTrialBalance(any(LocalDate.class));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display trial balance with specified date")
        void trialBalance_SpecifiedDate_ReturnsReport() throws Exception {
            LocalDate testDate = LocalDate.of(2026, 1, 15);
            when(reportService.generateTrialBalance(testDate)).thenReturn(trialBalanceDTO);

            mockMvc.perform(get("/reports/trial-balance")
                            .param("asOfDate", "2026-01-15"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("reports/trial-balance"))
                    .andExpect(model().attribute("asOfDate", testDate));

            verify(reportService).generateTrialBalance(testDate);
        }
    }

    @Nested
    @DisplayName("Profit & Loss Report")
    class ProfitLossReport {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display profit & loss with default dates")
        void profitLoss_DefaultDates_ReturnsReport() throws Exception {
            when(reportService.generateProfitLoss(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(profitLossDTO);

            mockMvc.perform(get("/reports/profit-loss"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("reports/profit-loss"))
                    .andExpect(model().attributeExists("profitLoss"))
                    .andExpect(model().attributeExists("startDate"))
                    .andExpect(model().attributeExists("endDate"));

            verify(reportService).generateProfitLoss(any(LocalDate.class), any(LocalDate.class));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display profit & loss with specified dates")
        void profitLoss_SpecifiedDates_ReturnsReport() throws Exception {
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 1, 31);
            when(reportService.generateProfitLoss(startDate, endDate)).thenReturn(profitLossDTO);

            mockMvc.perform(get("/reports/profit-loss")
                            .param("startDate", "2026-01-01")
                            .param("endDate", "2026-01-31"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("reports/profit-loss"))
                    .andExpect(model().attribute("startDate", startDate))
                    .andExpect(model().attribute("endDate", endDate));

            verify(reportService).generateProfitLoss(startDate, endDate);
        }
    }

    @Nested
    @DisplayName("Balance Sheet Report")
    class BalanceSheetReport {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display balance sheet with default date")
        void balanceSheet_DefaultDate_ReturnsReport() throws Exception {
            when(reportService.generateBalanceSheet(any(LocalDate.class))).thenReturn(balanceSheetDTO);

            mockMvc.perform(get("/reports/balance-sheet"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("reports/balance-sheet"))
                    .andExpect(model().attributeExists("balanceSheet"))
                    .andExpect(model().attributeExists("asOfDate"));

            verify(reportService).generateBalanceSheet(any(LocalDate.class));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display balance sheet with specified date")
        void balanceSheet_SpecifiedDate_ReturnsReport() throws Exception {
            LocalDate testDate = LocalDate.of(2026, 1, 31);
            when(reportService.generateBalanceSheet(testDate)).thenReturn(balanceSheetDTO);

            mockMvc.perform(get("/reports/balance-sheet")
                            .param("asOfDate", "2026-01-31"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("reports/balance-sheet"))
                    .andExpect(model().attribute("asOfDate", testDate));

            verify(reportService).generateBalanceSheet(testDate);
        }
    }

    @Nested
    @DisplayName("General Ledger Report")
    class GeneralLedgerReport {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display general ledger form without account selected")
        void generalLedger_NoAccountSelected_ReturnsFormView() throws Exception {
            when(accountService.findAllActive()).thenReturn(Arrays.asList(cashAccount));

            mockMvc.perform(get("/reports/general-ledger"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("reports/general-ledger"))
                    .andExpect(model().attributeExists("accounts"))
                    .andExpect(model().attributeExists("startDate"))
                    .andExpect(model().attributeExists("endDate"));

            verify(accountService).findAllActive();
            verify(ledgerService, never()).generateLedger(anyLong(), any(), any());
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display general ledger with account selected")
        void generalLedger_AccountSelected_ReturnsLedgerData() throws Exception {
            LedgerDTO ledgerDTO = new LedgerDTO();
            ledgerDTO.setAccountId(1L);
            ledgerDTO.setAccountCode("1000");
            ledgerDTO.setAccountName("Cash");
            ledgerDTO.setOpeningBalance(BigDecimal.ZERO);
            ledgerDTO.setClosingBalance(BigDecimal.valueOf(1000));

            when(accountService.findAllActive()).thenReturn(Arrays.asList(cashAccount));
            when(ledgerService.generateLedger(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(ledgerDTO);

            mockMvc.perform(get("/reports/general-ledger")
                            .param("accountId", "1")
                            .param("startDate", "2026-01-01")
                            .param("endDate", "2026-01-31"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("reports/general-ledger"))
                    .andExpect(model().attributeExists("ledger"))
                    .andExpect(model().attribute("selectedAccountId", 1L));

            verify(ledgerService).generateLedger(eq(1L), any(LocalDate.class), any(LocalDate.class));
        }
    }
}