package com.accounting.automation.pages;

import com.accounting.automation.config.ConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

/**
 * Journal Page Object - Journal Entries
 */
@Slf4j
public class JournalPage extends BasePage {

    @FindBy(css = "h2")
    private WebElement pageTitle;

    @FindBy(css = "a[href*='/journal/new']")
    private WebElement newEntryButton;

    @FindBy(css = "table.table tbody tr")
    private List<WebElement> entryRows;

    @FindBy(css = ".alert-success")
    private WebElement successMessage;

    // Form fields
    @FindBy(id = "entryDate")
    private WebElement entryDateField;

    @FindBy(id = "reference")
    private WebElement referenceField;

    @FindBy(id = "description")
    private WebElement descriptionField;

    @FindBy(css = "button.add-line, #addLine")
    private WebElement addLineButton;

    @FindBy(css = "button[type='submit']")
    private WebElement saveButton;

    @FindBy(css = ".debit-account, select[name*='debitAccount']")
    private List<WebElement> debitAccountSelects;

    @FindBy(css = ".credit-account, select[name*='creditAccount']")
    private List<WebElement> creditAccountSelects;

    @FindBy(css = ".debit-amount, input[name*='debitAmount']")
    private List<WebElement> debitAmountFields;

    @FindBy(css = ".credit-amount, input[name*='creditAmount']")
    private List<WebElement> creditAmountFields;

    public JournalPage() {
        super();
    }

    public JournalPage open() {
        navigateTo(ConfigReader.getBaseUrl() + "/journal");
        waitForPageLoad();
        log.info("Opened journal page");
        return this;
    }

    public boolean isJournalPageDisplayed() {
        return getCurrentUrl().contains("/journal");
    }

    public String getPageTitle() {
        return getText(pageTitle);
    }

    public int getEntryCount() {
        return entryRows.size();
    }

    public JournalPage clickNewEntryButton() {
        click(newEntryButton);
        waitForPageLoad();
        log.info("Clicked New Entry button");
        return this;
    }

    public JournalPage enterEntryDate(String date) {
        type(entryDateField, date);
        return this;
    }

    public JournalPage enterReference(String reference) {
        type(referenceField, reference);
        return this;
    }

    public JournalPage enterDescription(String description) {
        type(descriptionField, description);
        return this;
    }

    public JournalPage addLine() {
        click(addLineButton);
        return this;
    }

    public JournalPage selectDebitAccount(int lineIndex, String account) {
        if (lineIndex < debitAccountSelects.size()) {
            selectByVisibleText(debitAccountSelects.get(lineIndex), account);
        }
        return this;
    }

    public JournalPage selectCreditAccount(int lineIndex, String account) {
        if (lineIndex < creditAccountSelects.size()) {
            selectByVisibleText(creditAccountSelects.get(lineIndex), account);
        }
        return this;
    }

    public JournalPage enterDebitAmount(int lineIndex, String amount) {
        if (lineIndex < debitAmountFields.size()) {
            type(debitAmountFields.get(lineIndex), amount);
        }
        return this;
    }

    public JournalPage enterCreditAmount(int lineIndex, String amount) {
        if (lineIndex < creditAmountFields.size()) {
            type(creditAmountFields.get(lineIndex), amount);
        }
        return this;
    }

    public JournalPage clickSaveButton() {
        click(saveButton);
        waitForPageLoad();
        return this;
    }

    public JournalPage createSimpleEntry(String date, String reference, String description,
                                          String debitAccount, String creditAccount, String amount) {
        clickNewEntryButton();
        enterEntryDate(date);
        enterReference(reference);
        enterDescription(description);
        selectDebitAccount(0, debitAccount);
        enterDebitAmount(0, amount);
        selectCreditAccount(0, creditAccount);
        enterCreditAmount(0, amount);
        clickSaveButton();
        log.info("Created journal entry: {}", reference);
        return this;
    }

    public JournalPage viewEntry(String reference) {
        for (WebElement row : entryRows) {
            if (row.getText().contains(reference)) {
                row.findElement(By.cssSelector("a[href*='/view']")).click();
                break;
            }
        }
        waitForPageLoad();
        return this;
    }

    public JournalPage postEntry(String reference) {
        for (WebElement row : entryRows) {
            if (row.getText().contains(reference)) {
                WebElement postButton = row.findElement(By.cssSelector("button[title='Post'], form[action*='post'] button"));
                click(postButton);
                break;
            }
        }
        waitForPageLoad();
        return this;
    }

    public boolean isSuccessMessageDisplayed() {
        return isDisplayed(successMessage);
    }

    public String getSuccessMessage() {
        return getText(successMessage);
    }

    public boolean isEntryDisplayed(String reference) {
        for (WebElement row : entryRows) {
            if (row.getText().contains(reference)) {
                return true;
            }
        }
        return false;
    }
}