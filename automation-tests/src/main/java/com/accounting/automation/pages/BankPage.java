package com.accounting.automation.pages;

import com.accounting.automation.config.ConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

/**
 * Bank Reconciliation Page Object
 */
@Slf4j
public class BankPage extends BasePage {

    @FindBy(css = "h2")
    private WebElement pageTitle;

    @FindBy(css = "a[href*='/bank/accounts']")
    private WebElement bankAccountsLink;

    @FindBy(css = "a[href*='/bank/reconciliation']")
    private WebElement reconciliationLink;

    @FindBy(css = "a[href*='/bank/import']")
    private WebElement importStatementLink;

    @FindBy(id = "bankAccountId")
    private WebElement bankAccountSelect;

    @FindBy(id = "statementDate")
    private WebElement statementDateField;

    @FindBy(id = "statementBalance")
    private WebElement statementBalanceField;

    @FindBy(css = "table.table tbody tr")
    private List<WebElement> transactionRows;

    @FindBy(css = "input[type='checkbox']")
    private List<WebElement> reconcileCheckboxes;

    @FindBy(css = "button[type='submit']")
    private WebElement submitButton;

    @FindBy(css = ".alert-success")
    private WebElement successMessage;

    @FindBy(css = ".difference, .reconciliation-difference")
    private WebElement reconciliationDifference;

    public BankPage() {
        super();
    }

    public BankPage open() {
        navigateTo(ConfigReader.getBaseUrl() + "/bank");
        waitForPageLoad();
        log.info("Opened bank page");
        return this;
    }

    public boolean isBankPageDisplayed() {
        return getCurrentUrl().contains("/bank");
    }

    public String getPageTitle() {
        return getText(pageTitle);
    }

    public BankPage openBankAccounts() {
        click(bankAccountsLink);
        waitForPageLoad();
        return this;
    }

    public BankPage openReconciliation() {
        click(reconciliationLink);
        waitForPageLoad();
        return this;
    }

    public BankPage openImportStatement() {
        click(importStatementLink);
        waitForPageLoad();
        return this;
    }

    public BankPage selectBankAccount(String accountName) {
        selectByVisibleText(bankAccountSelect, accountName);
        return this;
    }

    public BankPage enterStatementDate(String date) {
        type(statementDateField, date);
        return this;
    }

    public BankPage enterStatementBalance(String balance) {
        type(statementBalanceField, balance);
        return this;
    }

    public BankPage reconcileTransaction(int index) {
        if (index < reconcileCheckboxes.size()) {
            click(reconcileCheckboxes.get(index));
        }
        return this;
    }

    public BankPage reconcileAllTransactions() {
        for (WebElement checkbox : reconcileCheckboxes) {
            if (!checkbox.isSelected()) {
                click(checkbox);
            }
        }
        return this;
    }

    public BankPage clickSubmitButton() {
        click(submitButton);
        waitForPageLoad();
        return this;
    }

    public int getTransactionCount() {
        return transactionRows.size();
    }

    public String getReconciliationDifference() {
        return getText(reconciliationDifference);
    }

    public boolean isReconciled() {
        String difference = getReconciliationDifference().replaceAll("[^0-9.]", "");
        return "0".equals(difference) || "0.00".equals(difference);
    }

    public boolean isSuccessMessageDisplayed() {
        return isDisplayed(successMessage);
    }

    public String getSuccessMessage() {
        return getText(successMessage);
    }
}