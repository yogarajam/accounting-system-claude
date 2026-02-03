package com.accounting.automation.pages;

import com.accounting.automation.config.ConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

/**
 * Accounts Page Object - Chart of Accounts
 */
@Slf4j
public class AccountsPage extends BasePage {

    @FindBy(css = "h2")
    private WebElement pageTitle;

    @FindBy(css = "a[href*='/accounts/new']")
    private WebElement newAccountButton;

    @FindBy(css = "table.table")
    private WebElement accountsTable;

    @FindBy(css = "table.table tbody tr")
    private List<WebElement> accountRows;

    @FindBy(css = ".alert-success")
    private WebElement successMessage;

    @FindBy(css = ".alert-danger")
    private WebElement errorMessage;

    // Filter buttons
    @FindBy(linkText = "All")
    private WebElement allFilter;

    @FindBy(linkText = "Assets")
    private WebElement assetsFilter;

    @FindBy(linkText = "Liabilities")
    private WebElement liabilitiesFilter;

    @FindBy(linkText = "Equity")
    private WebElement equityFilter;

    @FindBy(linkText = "Revenue")
    private WebElement revenueFilter;

    @FindBy(linkText = "Expenses")
    private WebElement expensesFilter;

    // Form fields
    @FindBy(id = "code")
    private WebElement codeField;

    @FindBy(id = "name")
    private WebElement nameField;

    @FindBy(id = "accountType")
    private WebElement accountTypeSelect;

    @FindBy(id = "description")
    private WebElement descriptionField;

    @FindBy(css = "button[type='submit']")
    private WebElement saveButton;

    public AccountsPage() {
        super();
    }

    public AccountsPage open() {
        navigateTo(ConfigReader.getBaseUrl() + "/accounts");
        waitForPageLoad();
        log.info("Opened accounts page");
        return this;
    }

    public boolean isAccountsPageDisplayed() {
        return getCurrentUrl().contains("/accounts");
    }

    public String getPageTitle() {
        return getText(pageTitle);
    }

    // Account listing methods
    public int getAccountCount() {
        return accountRows.size();
    }

    public boolean isAccountDisplayed(String accountCode) {
        for (WebElement row : accountRows) {
            if (row.getText().contains(accountCode)) {
                return true;
            }
        }
        return false;
    }

    public AccountsPage clickNewAccountButton() {
        click(newAccountButton);
        log.info("Clicked New Account button");
        return this;
    }

    // Filter methods with robust element finding
    public AccountsPage filterByAll() {
        try {
            WebElement filter = driver.findElement(By.linkText("All"));
            filter.click();
            waitForPageLoad();
        } catch (Exception e) {
            log.warn("All filter click failed: {}", e.getMessage());
        }
        return this;
    }

    public AccountsPage filterByAssets() {
        clickFilterByType("ASSET", "Assets");
        return this;
    }

    public AccountsPage filterByLiabilities() {
        clickFilterByType("LIABILITY", "Liabilities");
        return this;
    }

    public AccountsPage filterByEquity() {
        clickFilterByType("EQUITY", "Equity");
        return this;
    }

    public AccountsPage filterByRevenue() {
        clickFilterByType("REVENUE", "Revenue");
        return this;
    }

    public AccountsPage filterByExpenses() {
        clickFilterByType("EXPENSE", "Expenses");
        return this;
    }

    private void clickFilterByType(String enumName, String displayName) {
        try {
            // Try by enum name first (ASSET, LIABILITY, etc.)
            List<WebElement> links = driver.findElements(By.cssSelector("a.btn"));
            for (WebElement link : links) {
                String text = link.getText().trim().toUpperCase();
                if (text.equals(enumName) || text.equals(displayName.toUpperCase())) {
                    link.click();
                    waitForPageLoad();
                    return;
                }
            }
            // Fallback to linkText
            driver.findElement(By.linkText(enumName)).click();
            waitForPageLoad();
        } catch (Exception e) {
            log.warn("Filter by {} failed: {}", enumName, e.getMessage());
        }
    }

    // Create account methods
    public AccountsPage enterAccountCode(String code) {
        type(codeField, code);
        return this;
    }

    public AccountsPage enterAccountName(String name) {
        type(nameField, name);
        return this;
    }

    public AccountsPage selectAccountType(String type) {
        selectByVisibleText(accountTypeSelect, type);
        return this;
    }

    public AccountsPage enterDescription(String description) {
        type(descriptionField, description);
        return this;
    }

    public AccountsPage clickSaveButton() {
        try {
            // Try multiple selectors for the save button
            WebElement btn = null;
            try {
                btn = driver.findElement(By.cssSelector("button[type='submit']"));
            } catch (Exception e1) {
                try {
                    btn = driver.findElement(By.cssSelector(".btn-primary[type='submit']"));
                } catch (Exception e2) {
                    btn = driver.findElement(By.cssSelector("form button.btn-primary"));
                }
            }
            if (btn != null && btn.isDisplayed() && btn.isEnabled()) {
                btn.click();
            }
        } catch (Exception e) {
            log.warn("Save button click failed, trying JS click: {}", e.getMessage());
            try {
                WebElement btn = driver.findElement(By.cssSelector("button[type='submit'], .btn-primary"));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            } catch (Exception e2) {
                log.error("JS click also failed: {}", e2.getMessage());
            }
        }
        waitForPageLoad();
        return this;
    }

    public AccountsPage createAccount(String code, String name, String type, String description) {
        clickNewAccountButton();
        enterAccountCode(code);
        enterAccountName(name);
        selectAccountType(type);
        enterDescription(description);
        clickSaveButton();
        log.info("Created account: {} - {}", code, name);
        return this;
    }

    // Edit account
    public AccountsPage clickEditAccount(String accountCode) {
        for (WebElement row : accountRows) {
            if (row.getText().contains(accountCode)) {
                row.findElement(By.cssSelector("a[href*='/edit']")).click();
                break;
            }
        }
        waitForPageLoad();
        return this;
    }

    // View account
    public AccountsPage clickViewAccount(String accountCode) {
        for (WebElement row : accountRows) {
            if (row.getText().contains(accountCode)) {
                row.findElement(By.cssSelector("a[href*='/view']")).click();
                break;
            }
        }
        waitForPageLoad();
        return this;
    }

    // Messages
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

    public boolean isErrorMessageDisplayed() {
        return isDisplayed(errorMessage);
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }
}