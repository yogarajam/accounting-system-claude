package com.accounting.automation.pages;

import com.accounting.automation.config.ConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Dashboard Page Object
 */
@Slf4j
public class DashboardPage extends BasePage {

    @FindBy(css = "h2, .page-title")
    private WebElement pageTitle;

    @FindBy(linkText = "Accounts")
    private WebElement accountsMenu;

    @FindBy(linkText = "Journal")
    private WebElement journalMenu;

    @FindBy(linkText = "Ledger")
    private WebElement ledgerMenu;

    @FindBy(linkText = "Invoices")
    private WebElement invoicesMenu;

    @FindBy(linkText = "Reports")
    private WebElement reportsMenu;

    @FindBy(linkText = "Bank")
    private WebElement bankMenu;

    @FindBy(css = "a[href*='logout']")
    private WebElement logoutLink;

    @FindBy(css = ".card-title:contains('Cash'), .widget-cash")
    private WebElement cashWidget;

    @FindBy(css = ".pending-entries, .card:contains('Pending')")
    private WebElement pendingEntriesWidget;

    @FindBy(css = ".overdue-invoices, .card:contains('Overdue')")
    private WebElement overdueInvoicesWidget;

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
        return getCurrentUrl().contains("dashboard") || isDisplayed(pageTitle);
    }

    public String getPageTitle() {
        return getText(pageTitle);
    }

    // Navigation methods
    public AccountsPage navigateToAccounts() {
        click(accountsMenu);
        log.info("Navigated to Accounts page");
        return new AccountsPage();
    }

    public JournalPage navigateToJournal() {
        click(journalMenu);
        log.info("Navigated to Journal page");
        return new JournalPage();
    }

    public LedgerPage navigateToLedger() {
        click(ledgerMenu);
        log.info("Navigated to Ledger page");
        return new LedgerPage();
    }

    public InvoicesPage navigateToInvoices() {
        click(invoicesMenu);
        log.info("Navigated to Invoices page");
        return new InvoicesPage();
    }

    public ReportsPage navigateToReports() {
        click(reportsMenu);
        log.info("Navigated to Reports page");
        return new ReportsPage();
    }

    public BankPage navigateToBank() {
        click(bankMenu);
        log.info("Navigated to Bank page");
        return new BankPage();
    }

    public LoginPage logout() {
        click(logoutLink);
        log.info("Logged out");
        return new LoginPage();
    }

    // Dashboard widget checks
    public boolean isCashWidgetDisplayed() {
        return isDisplayed(cashWidget);
    }

    public boolean isPendingEntriesWidgetDisplayed() {
        return isDisplayed(pendingEntriesWidget);
    }

    public boolean isOverdueInvoicesWidgetDisplayed() {
        return isDisplayed(overdueInvoicesWidget);
    }
}