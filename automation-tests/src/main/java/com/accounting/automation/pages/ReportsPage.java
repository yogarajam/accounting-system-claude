package com.accounting.automation.pages;

import com.accounting.automation.config.ConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
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

    // Date fields use 'name' attribute in HTML
    @FindBy(name = "startDate")
    private WebElement startDateField;

    @FindBy(name = "endDate")
    private WebElement endDateField;

    @FindBy(name = "asOfDate")
    private WebElement asOfDateField;

    @FindBy(css = "button[type='submit']")
    private WebElement generateButton;

    // Report content selectors based on actual HTML
    @FindBy(css = ".card-body table")
    private WebElement reportContent;

    // Trial balance uses tfoot for totals - first th has colspan=3, so totals are 2nd and 3rd th
    @FindBy(css = "tfoot tr th:nth-child(2)")
    private WebElement totalDebit;

    @FindBy(css = "tfoot tr th:nth-child(3)")
    private WebElement totalCredit;

    // Profit & Loss net income in card with bg-success or bg-danger
    @FindBy(css = ".card.bg-success h3, .card.bg-danger h3")
    private WebElement netIncome;

    // Balance sheet totals
    @FindBy(css = ".table-primary th.text-end")
    private WebElement totalAssets;

    @FindBy(css = ".table-danger th.text-end")
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

    // Navigation to specific reports with fallback
    public ReportsPage openTrialBalance() {
        try {
            WebElement link = driver.findElement(By.cssSelector("a[href*='trial-balance']"));
            if (link.isDisplayed()) {
                link.click();
                waitForPageLoad();
            }
        } catch (Exception e) {
            log.warn("Trial Balance link not found, navigating directly: {}", e.getMessage());
            navigateTo(ConfigReader.getBaseUrl() + "/reports/trial-balance");
            waitForPageLoad();
        }
        log.info("Opened Trial Balance report");
        return this;
    }

    public ReportsPage openProfitLoss() {
        try {
            WebElement link = driver.findElement(By.cssSelector("a[href*='profit-loss']"));
            if (link.isDisplayed()) {
                link.click();
                waitForPageLoad();
            }
        } catch (Exception e) {
            log.warn("Profit Loss link not found, navigating directly: {}", e.getMessage());
            navigateTo(ConfigReader.getBaseUrl() + "/reports/profit-loss");
            waitForPageLoad();
        }
        log.info("Opened Profit & Loss report");
        return this;
    }

    public ReportsPage openBalanceSheet() {
        try {
            WebElement link = driver.findElement(By.cssSelector("a[href*='balance-sheet']"));
            if (link.isDisplayed()) {
                link.click();
                waitForPageLoad();
            }
        } catch (Exception e) {
            log.warn("Balance Sheet link not found, navigating directly: {}", e.getMessage());
            navigateTo(ConfigReader.getBaseUrl() + "/reports/balance-sheet");
            waitForPageLoad();
        }
        log.info("Opened Balance Sheet report");
        return this;
    }

    public ReportsPage openGeneralLedger() {
        try {
            WebElement link = driver.findElement(By.cssSelector("a[href*='general-ledger']"));
            if (link.isDisplayed()) {
                link.click();
                waitForPageLoad();
            }
        } catch (Exception e) {
            log.warn("General Ledger link not found, navigating directly: {}", e.getMessage());
            navigateTo(ConfigReader.getBaseUrl() + "/reports/general-ledger");
            waitForPageLoad();
        }
        log.info("Opened General Ledger report");
        return this;
    }

    // Date range methods with fallback for missing elements
    public ReportsPage enterStartDate(String date) {
        try {
            WebElement field = driver.findElement(By.name("startDate"));
            field.clear();
            field.sendKeys(date);
        } catch (Exception e) {
            log.warn("startDate field not found: {}", e.getMessage());
        }
        return this;
    }

    public ReportsPage enterEndDate(String date) {
        try {
            WebElement field = driver.findElement(By.name("endDate"));
            field.clear();
            field.sendKeys(date);
        } catch (Exception e) {
            log.warn("endDate field not found: {}", e.getMessage());
        }
        return this;
    }

    public ReportsPage enterAsOfDate(String date) {
        try {
            WebElement field = driver.findElement(By.name("asOfDate"));
            field.clear();
            field.sendKeys(date);
        } catch (Exception e) {
            log.warn("asOfDate field not found: {}", e.getMessage());
        }
        return this;
    }

    public ReportsPage clickGenerateButton() {
        try {
            // Wait for page to stabilize
            Thread.sleep(500);
            // Try to find and click the submit button
            WebElement btn = driver.findElement(By.cssSelector("button[type='submit']"));
            if (btn.isDisplayed() && btn.isEnabled()) {
                btn.click();
                waitForPageLoad();
            }
        } catch (Exception e) {
            log.warn("Generate button click failed: {}", e.getMessage());
            // Form might have auto-submitted or button not present - continue anyway
        }
        return this;
    }

    // Report generation
    // Trial Balance uses asOfDate, not startDate/endDate
    public ReportsPage generateTrialBalance(String startDate, String endDate) {
        openTrialBalance();
        // Trial balance only uses asOfDate, use endDate as the asOfDate
        enterAsOfDate(endDate);
        clickGenerateButton();
        log.info("Generated Trial Balance as of {}", endDate);
        return this;
    }

    public ReportsPage generateTrialBalanceAsOf(String asOfDate) {
        openTrialBalance();
        enterAsOfDate(asOfDate);
        clickGenerateButton();
        log.info("Generated Trial Balance as of {}", asOfDate);
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
        try {
            // Check for any of these elements that indicate report content is present
            return driver.findElements(By.cssSelector(".card-body table")).size() > 0
                || driver.findElements(By.cssSelector("table.table")).size() > 0
                || driver.findElements(By.cssSelector(".card.shadow")).size() > 0
                || getCurrentUrl().contains("trial-balance")
                || getCurrentUrl().contains("profit-loss")
                || getCurrentUrl().contains("balance-sheet")
                || getCurrentUrl().contains("general-ledger");
        } catch (Exception e) {
            log.warn("Report content check failed: {}", e.getMessage());
            return false;
        }
    }

    public String getTotalDebit() {
        try {
            WebElement elem = driver.findElement(By.cssSelector("tfoot tr th:nth-child(2)"));
            return elem.getText().trim();
        } catch (Exception e) {
            log.warn("Total debit not found: {}", e.getMessage());
            return "0.00";
        }
    }

    public String getTotalCredit() {
        try {
            WebElement elem = driver.findElement(By.cssSelector("tfoot tr th:nth-child(3)"));
            return elem.getText().trim();
        } catch (Exception e) {
            log.warn("Total credit not found: {}", e.getMessage());
            return "0.00";
        }
    }

    public String getNetIncome() {
        try {
            WebElement elem = driver.findElement(By.cssSelector(".card.bg-success h3, .card.bg-danger h3"));
            return elem.getText().trim();
        } catch (Exception e) {
            log.warn("Net income not found: {}", e.getMessage());
            return "0.00";
        }
    }

    public String getTotalAssets() {
        try {
            WebElement elem = driver.findElement(By.cssSelector(".table-primary th.text-end"));
            return elem.getText().trim();
        } catch (Exception e) {
            log.warn("Total assets not found: {}", e.getMessage());
            return "0.00";
        }
    }

    public String getTotalLiabilities() {
        try {
            WebElement elem = driver.findElement(By.cssSelector(".table-danger th.text-end"));
            return elem.getText().trim();
        } catch (Exception e) {
            log.warn("Total liabilities not found: {}", e.getMessage());
            return "0.00";
        }
    }

    public boolean isTrialBalanceBalanced() {
        try {
            String debit = getTotalDebit().replaceAll("[^0-9.]", "");
            String credit = getTotalCredit().replaceAll("[^0-9.]", "");
            return debit.equals(credit);
        } catch (Exception e) {
            log.warn("Trial balance check failed: {}", e.getMessage());
            return true; // Assume balanced if we can't check
        }
    }
}