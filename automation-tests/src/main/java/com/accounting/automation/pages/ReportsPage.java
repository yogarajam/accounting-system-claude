package com.accounting.automation.pages;

import com.accounting.automation.config.ConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Reports Page Object
 */
@Slf4j
public class ReportsPage extends BasePage {

    @FindBy(css = "h2")
    private WebElement pageTitle;

    @FindBy(css = "a[href*='trial-balance']")
    private WebElement trialBalanceLink;

    @FindBy(css = "a[href*='profit-loss']")
    private WebElement profitLossLink;

    @FindBy(css = "a[href*='balance-sheet']")
    private WebElement balanceSheetLink;

    @FindBy(css = "a[href*='general-ledger']")
    private WebElement generalLedgerLink;

    @FindBy(id = "startDate")
    private WebElement startDateField;

    @FindBy(id = "endDate")
    private WebElement endDateField;

    @FindBy(id = "asOfDate")
    private WebElement asOfDateField;

    @FindBy(css = "button[type='submit']")
    private WebElement generateButton;

    @FindBy(css = ".report-content, .report-table")
    private WebElement reportContent;

    @FindBy(css = ".total-debit")
    private WebElement totalDebit;

    @FindBy(css = ".total-credit")
    private WebElement totalCredit;

    @FindBy(css = ".net-income, .net-profit")
    private WebElement netIncome;

    @FindBy(css = ".total-assets")
    private WebElement totalAssets;

    @FindBy(css = ".total-liabilities")
    private WebElement totalLiabilities;

    public ReportsPage() {
        super();
    }

    public ReportsPage open() {
        navigateTo(ConfigReader.getBaseUrl() + "/reports");
        waitForPageLoad();
        log.info("Opened reports page");
        return this;
    }

    public boolean isReportsPageDisplayed() {
        return getCurrentUrl().contains("/reports");
    }

    public String getPageTitle() {
        return getText(pageTitle);
    }

    // Navigation to specific reports
    public ReportsPage openTrialBalance() {
        click(trialBalanceLink);
        waitForPageLoad();
        log.info("Opened Trial Balance report");
        return this;
    }

    public ReportsPage openProfitLoss() {
        click(profitLossLink);
        waitForPageLoad();
        log.info("Opened Profit & Loss report");
        return this;
    }

    public ReportsPage openBalanceSheet() {
        click(balanceSheetLink);
        waitForPageLoad();
        log.info("Opened Balance Sheet report");
        return this;
    }

    public ReportsPage openGeneralLedger() {
        click(generalLedgerLink);
        waitForPageLoad();
        log.info("Opened General Ledger report");
        return this;
    }

    // Date range methods
    public ReportsPage enterStartDate(String date) {
        type(startDateField, date);
        return this;
    }

    public ReportsPage enterEndDate(String date) {
        type(endDateField, date);
        return this;
    }

    public ReportsPage enterAsOfDate(String date) {
        type(asOfDateField, date);
        return this;
    }

    public ReportsPage clickGenerateButton() {
        click(generateButton);
        waitForPageLoad();
        return this;
    }

    // Report generation
    public ReportsPage generateTrialBalance(String startDate, String endDate) {
        openTrialBalance();
        enterStartDate(startDate);
        enterEndDate(endDate);
        clickGenerateButton();
        log.info("Generated Trial Balance for {} to {}", startDate, endDate);
        return this;
    }

    public ReportsPage generateProfitLoss(String startDate, String endDate) {
        openProfitLoss();
        enterStartDate(startDate);
        enterEndDate(endDate);
        clickGenerateButton();
        log.info("Generated Profit & Loss for {} to {}", startDate, endDate);
        return this;
    }

    public ReportsPage generateBalanceSheet(String asOfDate) {
        openBalanceSheet();
        enterAsOfDate(asOfDate);
        clickGenerateButton();
        log.info("Generated Balance Sheet as of {}", asOfDate);
        return this;
    }

    // Report content checks
    public boolean isReportContentDisplayed() {
        return isDisplayed(reportContent);
    }

    public String getTotalDebit() {
        return getText(totalDebit);
    }

    public String getTotalCredit() {
        return getText(totalCredit);
    }

    public String getNetIncome() {
        return getText(netIncome);
    }

    public String getTotalAssets() {
        return getText(totalAssets);
    }

    public String getTotalLiabilities() {
        return getText(totalLiabilities);
    }

    public boolean isTrialBalanceBalanced() {
        String debit = getTotalDebit().replaceAll("[^0-9.]", "");
        String credit = getTotalCredit().replaceAll("[^0-9.]", "");
        return debit.equals(credit);
    }
}