package com.accounting.api.client;

import com.accounting.api.model.ReportDTO;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Report API Client
 * Handles all report-related API calls
 */
@Slf4j
public class ReportApiClient extends BaseApiClient {

    private static final String REPORTS_API_PATH = "/api/reports";
    private static final String REPORTS_WEB_PATH = "/reports";

    // API Endpoints (JSON)
    public Response getTrialBalance(LocalDate asOfDate) {
        Map<String, String> params = new HashMap<>();
        params.put("asOfDate", asOfDate.toString());
        return get(REPORTS_API_PATH + "/trial-balance", params);
    }

    public Response getProfitLoss(LocalDate startDate, LocalDate endDate) {
        Map<String, String> params = new HashMap<>();
        params.put("startDate", startDate.toString());
        params.put("endDate", endDate.toString());
        return get(REPORTS_API_PATH + "/profit-loss", params);
    }

    public Response getBalanceSheet(LocalDate asOfDate) {
        Map<String, String> params = new HashMap<>();
        params.put("asOfDate", asOfDate.toString());
        return get(REPORTS_API_PATH + "/balance-sheet", params);
    }

    public Response getGeneralLedger(Long accountId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("accountId", accountId);
        params.put("startDate", startDate.toString());
        params.put("endDate", endDate.toString());
        return get(REPORTS_API_PATH + "/general-ledger", params);
    }

    // Web Endpoints
    public Response getReportsIndexPage() {
        return get(REPORTS_WEB_PATH);
    }

    public Response getTrialBalancePage() {
        return get(REPORTS_WEB_PATH + "/trial-balance");
    }

    public Response getTrialBalancePage(LocalDate asOfDate) {
        Map<String, String> params = new HashMap<>();
        params.put("asOfDate", asOfDate.toString());
        return get(REPORTS_WEB_PATH + "/trial-balance", params);
    }

    public Response getProfitLossPage() {
        return get(REPORTS_WEB_PATH + "/profit-loss");
    }

    public Response getProfitLossPage(LocalDate startDate, LocalDate endDate) {
        Map<String, String> params = new HashMap<>();
        params.put("startDate", startDate.toString());
        params.put("endDate", endDate.toString());
        return get(REPORTS_WEB_PATH + "/profit-loss", params);
    }

    public Response getBalanceSheetPage() {
        return get(REPORTS_WEB_PATH + "/balance-sheet");
    }

    public Response getBalanceSheetPage(LocalDate asOfDate) {
        Map<String, String> params = new HashMap<>();
        params.put("asOfDate", asOfDate.toString());
        return get(REPORTS_WEB_PATH + "/balance-sheet", params);
    }

    public Response getGeneralLedgerPage() {
        return get(REPORTS_WEB_PATH + "/general-ledger");
    }

    public Response getGeneralLedgerPage(Long accountId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("accountId", accountId);
        params.put("startDate", startDate.toString());
        params.put("endDate", endDate.toString());
        return get(REPORTS_WEB_PATH + "/general-ledger", params);
    }

    // Helper methods
    public ReportDTO.TrialBalanceDTO getTrialBalanceDTO(LocalDate asOfDate) {
        return getTrialBalance(asOfDate)
                .then()
                .extract()
                .as(ReportDTO.TrialBalanceDTO.class);
    }

    public ReportDTO.ProfitLossDTO getProfitLossDTO(LocalDate startDate, LocalDate endDate) {
        return getProfitLoss(startDate, endDate)
                .then()
                .extract()
                .as(ReportDTO.ProfitLossDTO.class);
    }

    public ReportDTO.BalanceSheetDTO getBalanceSheetDTO(LocalDate asOfDate) {
        return getBalanceSheet(asOfDate)
                .then()
                .extract()
                .as(ReportDTO.BalanceSheetDTO.class);
    }

    public ReportDTO.LedgerDTO getGeneralLedgerDTO(Long accountId, LocalDate startDate, LocalDate endDate) {
        return getGeneralLedger(accountId, startDate, endDate)
                .then()
                .extract()
                .as(ReportDTO.LedgerDTO.class);
    }
}