package com.accounting.controller;

import com.accounting.dto.DashboardDTO;
import com.accounting.service.ReportService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@DisplayName("DashboardController Integration Tests")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    private DashboardDTO dashboardDTO;

    @BeforeEach
    void setUp() {
        dashboardDTO = createDashboardDTO();
    }

    private DashboardDTO createDashboardDTO() {
        DashboardDTO dto = new DashboardDTO();
        dto.setTotalAssets(BigDecimal.valueOf(100000));
        dto.setTotalLiabilities(BigDecimal.valueOf(40000));
        dto.setTotalEquity(BigDecimal.valueOf(60000));
        dto.setTotalRevenue(BigDecimal.valueOf(50000));
        dto.setTotalExpenses(BigDecimal.valueOf(30000));
        dto.setNetIncome(BigDecimal.valueOf(20000));
        dto.setCashBalance(BigDecimal.valueOf(25000));
        dto.setAccountsReceivable(BigDecimal.valueOf(15000));
        dto.setAccountsPayable(BigDecimal.valueOf(10000));
        dto.setPendingJournalEntries(5L);
        dto.setOverdueInvoices(2L);
        dto.setOverdueAmount(BigDecimal.valueOf(5000));
        return dto;
    }

    @Nested
    @DisplayName("Dashboard Home")
    class DashboardHome {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display dashboard at root path")
        void dashboard_RootPath_ReturnsDashboardView() throws Exception {
            when(reportService.generateDashboard()).thenReturn(dashboardDTO);

            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attributeExists("dashboard"));

            verify(reportService).generateDashboard();
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display dashboard at /dashboard path")
        void dashboard_DashboardPath_ReturnsDashboardView() throws Exception {
            when(reportService.generateDashboard()).thenReturn(dashboardDTO);

            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attributeExists("dashboard"));

            verify(reportService).generateDashboard();
        }

        @Test
        @DisplayName("Should return unauthorized when not authenticated at root")
        void dashboard_RootPath_NotAuthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return unauthorized when not authenticated at /dashboard")
        void dashboard_DashboardPath_NotAuthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Dashboard Data")
    class DashboardData {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should return dashboard with all financial metrics")
        void dashboard_ReturnsAllFinancialMetrics() throws Exception {
            when(reportService.generateDashboard()).thenReturn(dashboardDTO);

            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("dashboard", dashboardDTO));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should handle empty dashboard data gracefully")
        void dashboard_EmptyData_ReturnsEmptyDashboard() throws Exception {
            DashboardDTO emptyDashboard = new DashboardDTO();

            when(reportService.generateDashboard()).thenReturn(emptyDashboard);

            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attributeExists("dashboard"));
        }
    }

    @Nested
    @DisplayName("Access Control")
    class AccessControl {

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("Should allow admin access to dashboard")
        void dashboard_AdminUser_AllowsAccess() throws Exception {
            when(reportService.generateDashboard()).thenReturn(dashboardDTO);

            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"));
        }

        @Test
        @WithMockUser(username = "viewer", roles = {"VIEWER"})
        @DisplayName("Should allow viewer access to dashboard")
        void dashboard_ViewerUser_AllowsAccess() throws Exception {
            when(reportService.generateDashboard()).thenReturn(dashboardDTO);

            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"));
        }
    }
}