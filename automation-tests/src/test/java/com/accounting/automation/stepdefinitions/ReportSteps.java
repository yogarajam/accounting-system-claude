package com.accounting.automation.stepdefinitions;

import com.accounting.automation.pages.DashboardPage;
import com.accounting.automation.pages.ReportsPage;
import com.accounting.automation.utils.TestDataGenerator;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Report Step Definitions
 */
@Slf4j
public class ReportSteps {

    private DashboardPage dashboardPage;
    private ReportsPage reportsPage;

    @Given("I am on the reports page")
    public void iAmOnTheReportsPage() {
        dashboardPage = new DashboardPage();
        reportsPage = dashboardPage.navigateToReports();
        assertThat(reportsPage.isReportsPageDisplayed())
                .as("Reports page should be displayed")
                .isTrue();
    }

    @When("I navigate to the reports page")
    public void iNavigateToTheReportsPage() {
        dashboardPage = new DashboardPage();
        reportsPage = dashboardPage.navigateToReports();
    }

    @When("I open the Trial Balance report")
    public void iOpenTheTrialBalanceReport() {
        reportsPage.openTrialBalance();
    }

    @When("I open the Profit and Loss report")
    public void iOpenTheProfitAndLossReport() {
        reportsPage.openProfitLoss();
    }

    @When("I open the Balance Sheet report")
    public void iOpenTheBalanceSheetReport() {
        reportsPage.openBalanceSheet();
    }

    @When("I open the General Ledger report")
    public void iOpenTheGeneralLedgerReport() {
        reportsPage.openGeneralLedger();
    }

    @When("I generate Trial Balance for current month")
    public void iGenerateTrialBalanceForCurrentMonth() {
        reportsPage.generateTrialBalance(
                TestDataGenerator.getFirstDayOfMonth(),
                TestDataGenerator.getTodayDate()
        );
    }

    @When("I generate Trial Balance from {string} to {string}")
    public void iGenerateTrialBalanceFromTo(String startDate, String endDate) {
        reportsPage.generateTrialBalance(startDate, endDate);
    }

    @When("I generate Profit and Loss for current month")
    public void iGenerateProfitAndLossForCurrentMonth() {
        reportsPage.generateProfitLoss(
                TestDataGenerator.getFirstDayOfMonth(),
                TestDataGenerator.getTodayDate()
        );
    }

    @When("I generate Profit and Loss from {string} to {string}")
    public void iGenerateProfitAndLossFromTo(String startDate, String endDate) {
        reportsPage.generateProfitLoss(startDate, endDate);
    }

    @When("I generate Balance Sheet as of today")
    public void iGenerateBalanceSheetAsOfToday() {
        reportsPage.generateBalanceSheet(TestDataGenerator.getTodayDate());
    }

    @When("I generate Balance Sheet as of {string}")
    public void iGenerateBalanceSheetAsOf(String asOfDate) {
        reportsPage.generateBalanceSheet(asOfDate);
    }

    @Then("I should see the report content")
    public void iShouldSeeTheReportContent() {
        assertThat(reportsPage.isReportContentDisplayed())
                .as("Report content should be displayed")
                .isTrue();
    }

    @Then("the Trial Balance should be balanced")
    public void theTrialBalanceShouldBeBalanced() {
        assertThat(reportsPage.isTrialBalanceBalanced())
                .as("Trial Balance total debits should equal total credits")
                .isTrue();
    }

    @Then("I should see total debits equal to total credits")
    public void iShouldSeeTotalDebitsEqualToTotalCredits() {
        String totalDebit = reportsPage.getTotalDebit();
        String totalCredit = reportsPage.getTotalCredit();
        assertThat(totalDebit)
                .as("Total debits should equal total credits")
                .isEqualTo(totalCredit);
    }

    @Then("I should see the net income")
    public void iShouldSeeTheNetIncome() {
        String netIncome = reportsPage.getNetIncome();
        assertThat(netIncome)
                .as("Net income should be displayed")
                .isNotEmpty();
    }

    @Then("I should see total assets and liabilities")
    public void iShouldSeeTotalAssetsAndLiabilities() {
        assertThat(reportsPage.getTotalAssets())
                .as("Total assets should be displayed")
                .isNotEmpty();
        assertThat(reportsPage.getTotalLiabilities())
                .as("Total liabilities should be displayed")
                .isNotEmpty();
    }
}