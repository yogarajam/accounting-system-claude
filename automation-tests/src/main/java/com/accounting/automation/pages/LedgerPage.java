package com.accounting.automation.pages;

import com.accounting.automation.config.ConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

/**
 * Ledger Page Object
 */
@Slf4j
public class LedgerPage extends BasePage {

    @FindBy(css = "h2")
    private WebElement pageTitle;

    @FindBy(id = "accountId")
    private WebElement accountSelect;

    @FindBy(id = "startDate")
    private WebElement startDateField;

    @FindBy(id = "endDate")
    private WebElement endDateField;

    @FindBy(css = "button[type='submit']")
    private WebElement viewLedgerButton;

    @FindBy(css = "table.table tbody tr")
    private List<WebElement> ledgerRows;

    @FindBy(css = ".opening-balance, .balance-row:first-child")
    private WebElement openingBalance;

    @FindBy(css = ".closing-balance, .balance-row:last-child")
    private WebElement closingBalance;

    public LedgerPage() {
        super();
    }

    public LedgerPage open() {
        navigateTo(ConfigReader.getBaseUrl() + "/ledger");
        waitForPageLoad();
        log.info("Opened ledger page");
        return this;
    }

    public boolean isLedgerPageDisplayed() {
        return getCurrentUrl().contains("/ledger");
    }

    public String getPageTitle() {
        return getText(pageTitle);
    }

    public LedgerPage selectAccount(String accountName) {
        selectByVisibleText(accountSelect, accountName);
        log.info("Selected account: {}", accountName);
        return this;
    }

    public LedgerPage enterStartDate(String date) {
        type(startDateField, date);
        return this;
    }

    public LedgerPage enterEndDate(String date) {
        type(endDateField, date);
        return this;
    }

    public LedgerPage clickViewLedgerButton() {
        click(viewLedgerButton);
        waitForPageLoad();
        return this;
    }

    public LedgerPage viewLedger(String accountName, String startDate, String endDate) {
        selectAccount(accountName);
        enterStartDate(startDate);
        enterEndDate(endDate);
        clickViewLedgerButton();
        log.info("Viewing ledger for {} from {} to {}", accountName, startDate, endDate);
        return this;
    }

    public int getLedgerEntryCount() {
        return ledgerRows.size();
    }

    public String getOpeningBalance() {
        return getText(openingBalance);
    }

    public String getClosingBalance() {
        return getText(closingBalance);
    }

    public boolean hasLedgerEntries() {
        return !ledgerRows.isEmpty();
    }
}