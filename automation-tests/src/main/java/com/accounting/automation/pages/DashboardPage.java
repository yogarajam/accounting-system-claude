package com.accounting.automation.pages;

import com.accounting.automation.config.ConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Dashboard Page Object
 */
@Slf4j
public class DashboardPage extends BasePage {

    @FindBy(css = "h2")
    private WebElement pageTitle;

    // Sidebar navigation links
    @FindBy(css = "a[href*='/accounts']")
    private WebElement accountsMenu;

    @FindBy(css = "a[href*='/journal']")
    private WebElement journalMenu;

    @FindBy(css = "a[href*='/ledger']")
    private WebElement ledgerMenu;

    @FindBy(css = "a[href*='/invoices']")
    private WebElement invoicesMenu;

    @FindBy(css = "a[href*='/reports']")
    private WebElement reportsMenu;

    @FindBy(css = "a[href*='/bank']")
    private WebElement bankMenu;

    // Logout is in a dropdown form
    @FindBy(css = "form[action*='logout'] button")
    private WebElement logoutButton;

    @FindBy(id = "navbarDropdown")
    private WebElement userDropdown;

    // Dashboard widgets
    @FindBy(xpath = "//div[contains(text(),'Total Assets')]")
    private WebElement totalAssetsWidget;

    @FindBy(xpath = "//div[contains(text(),'Cash Balance')]")
    private WebElement cashBalanceWidget;

    @FindBy(xpath = "//div[contains(text(),'Pending Entries')]")
    private WebElement pendingEntriesWidget;

    public DashboardPage() {
        super();
    }

    public DashboardPage open() {
        navigateTo(ConfigReader.getBaseUrl() + "/dashboard");
        waitForPageLoad();
        log.info("Opened dashboard page");
        return this;
    }

    public boolean isDashboardDisplayed() {
        waitForPageLoad();
        try {
            // Check if we're on the dashboard by URL or page content
            String url = getCurrentUrl();
            if (url.contains("dashboard")) {
                return true;
            }
            // Also check if page title contains Dashboard
            if (isDisplayed(pageTitle) && getText(pageTitle).contains("Dashboard")) {
                return true;
            }
            // Check for sidebar presence (indicates logged in)
            return driver.findElements(By.id("sidebar-wrapper")).size() > 0;
        } catch (Exception e) {
            log.debug("Dashboard check failed: {}", e.getMessage());
            return false;
        }
    }

    public String getPageTitle() {
        return getText(pageTitle);
    }

    // Navigation methods
    public AccountsPage navigateToAccounts() {
        click(accountsMenu);
        waitForPageLoad();
        log.info("Navigated to Accounts page");
        return new AccountsPage();
    }

    public JournalPage navigateToJournal() {
        click(journalMenu);
        waitForPageLoad();
        log.info("Navigated to Journal page");
        return new JournalPage();
    }

    public LedgerPage navigateToLedger() {
        click(ledgerMenu);
        waitForPageLoad();
        log.info("Navigated to Ledger page");
        return new LedgerPage();
    }

    public InvoicesPage navigateToInvoices() {
        click(invoicesMenu);
        waitForPageLoad();
        log.info("Navigated to Invoices page");
        return new InvoicesPage();
    }

    public ReportsPage navigateToReports() {
        click(reportsMenu);
        waitForPageLoad();
        log.info("Navigated to Reports page");
        return new ReportsPage();
    }

    public BankPage navigateToBank() {
        click(bankMenu);
        waitForPageLoad();
        log.info("Navigated to Bank page");
        return new BankPage();
    }

    public LoginPage logout() {
        click(userDropdown);
        click(logoutButton);
        waitForPageLoad();
        log.info("Logged out");
        return new LoginPage();
    }

    // Dashboard widget checks
    public boolean isTotalAssetsWidgetDisplayed() {
        try {
            return isDisplayed(totalAssetsWidget);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCashBalanceWidgetDisplayed() {
        try {
            return isDisplayed(cashBalanceWidget);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPendingEntriesWidgetDisplayed() {
        try {
            return isDisplayed(pendingEntriesWidget);
        } catch (Exception e) {
            return false;
        }
    }
}