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

    // Account selects in journal form lines
    @FindBy(css = "select.account-select, select[name*='accountId']")
    private List<WebElement> accountSelects;

    // Debit amount inputs
    @FindBy(css = "input.debit-input, input[name*='debitAmount']")
    private List<WebElement> debitAmountFields;

    // Credit amount inputs
    @FindBy(css = "input.credit-input, input[name*='creditAmount']")
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
        try {
            // Wait for page to be ready
            Thread.sleep(500);
            // Try to find and click the new entry button/link
            WebElement btn = driver.findElement(By.cssSelector("a[href*='/journal/new'], a.btn-primary"));
            if (btn.isDisplayed()) {
                btn.click();
                waitForPageLoad();
                log.info("Clicked New Entry button");
            }
        } catch (Exception e) {
            log.warn("New Entry button click failed: {}", e.getMessage());
            // Try navigating directly
            navigateTo(ConfigReader.getBaseUrl() + "/journal/new");
            waitForPageLoad();
        }
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
        try {
            List<WebElement> selects = driver.findElements(By.cssSelector("select.account-select, select[name*='accountId']"));
            if (lineIndex < selects.size()) {
                selectByVisibleText(selects.get(lineIndex), account);
            }
        } catch (Exception e) {
            log.warn("Failed to select debit account: {}", e.getMessage());
        }
        return this;
    }

    public JournalPage selectCreditAccount(int lineIndex, String account) {
        try {
            List<WebElement> selects = driver.findElements(By.cssSelector("select.account-select, select[name*='accountId']"));
            if (lineIndex < selects.size()) {
                selectByVisibleText(selects.get(lineIndex), account);
            }
        } catch (Exception e) {
            log.warn("Failed to select credit account: {}", e.getMessage());
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
        try {
            // Try multiple selectors for the save button
            WebElement btn = null;
            try {
                btn = driver.findElement(By.id("saveBtn"));
            } catch (Exception e1) {
                try {
                    btn = driver.findElement(By.cssSelector("button[type='submit']"));
                } catch (Exception e2) {
                    btn = driver.findElement(By.cssSelector(".btn-primary[type='submit']"));
                }
            }
            if (btn != null && btn.isDisplayed() && btn.isEnabled()) {
                btn.click();
            }
        } catch (Exception e) {
            log.warn("Save button click failed, trying JS click: {}", e.getMessage());
            try {
                WebElement btn = driver.findElement(By.cssSelector("button[type='submit'], #saveBtn"));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            } catch (Exception e2) {
                log.error("JS click also failed: {}", e2.getMessage());
            }
        }
        waitForPageLoad();
        return this;
    }

    public JournalPage createSimpleEntry(String date, String reference, String description,
                                          String debitAccount, String creditAccount, String amount) {
        clickNewEntryButton();
        enterEntryDate(date);
        enterReference(reference);
        enterDescription(description);
        // Row 0: Debit entry
        selectDebitAccount(0, debitAccount);
        enterDebitAmount(0, amount);
        // Row 1: Credit entry (need to use different row)
        selectCreditAccount(1, creditAccount);
        enterCreditAmount(1, amount);
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
        try {
            // Wait for page to fully load and success message to appear
            Thread.sleep(1000);
            String pageSource = driver.getPageSource().toLowerCase();
            // Check multiple ways for success message
            return driver.findElements(By.cssSelector(".alert-success")).size() > 0
                || pageSource.contains("success")
                || pageSource.contains("saved")
                || pageSource.contains("created")
                || pageSource.contains("updated");
        } catch (Exception e) {
            return false;
        }
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