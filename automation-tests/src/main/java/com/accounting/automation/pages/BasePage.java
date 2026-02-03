package com.accounting.automation.pages;

import com.accounting.automation.config.ConfigReader;
import com.accounting.automation.config.DriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Base Page Object - Contains common methods for all page objects
 */
@Slf4j
public abstract class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getExplicitWait()));
        PageFactory.initElements(driver, this);
    }

    // Navigation
    public void navigateTo(String url) {
        log.info("Navigating to: {}", url);
        driver.get(url);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public void refreshPage() {
        driver.navigate().refresh();
    }

    // Wait methods
    protected WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForElementClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected WebElement waitForElementClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    protected void waitForElementInvisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void waitForTextPresent(By locator, String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    // Click methods
    protected void click(WebElement element) {
        waitForElementClickable(element).click();
        log.debug("Clicked element: {}", element);
    }

    protected void click(By locator) {
        waitForElementClickable(locator).click();
        log.debug("Clicked element: {}", locator);
    }

    protected void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        log.debug("JS clicked element: {}", element);
    }

    // Input methods with robust error handling
    protected void type(WebElement element, String text) {
        try {
            waitForElementClickable(element);
            element.clear();
            element.sendKeys(text);
            log.debug("Typed '{}' into element: {}", text, element);
        } catch (Exception e) {
            log.warn("Type failed with wait, trying direct: {}", e.getMessage());
            try {
                element.clear();
                element.sendKeys(text);
            } catch (Exception e2) {
                log.error("Type completely failed: {}", e2.getMessage());
            }
        }
    }

    protected void type(By locator, String text) {
        try {
            WebElement element = waitForElement(locator);
            element.clear();
            element.sendKeys(text);
            log.debug("Typed '{}' into element: {}", text, locator);
        } catch (Exception e) {
            log.warn("Type by locator failed: {}", e.getMessage());
        }
    }

    protected void clearAndType(WebElement element, String text) {
        element.clear();
        element.sendKeys(text);
    }

    // Select dropdown methods
    protected void selectByVisibleText(WebElement element, String text) {
        Select select = new Select(element);
        select.selectByVisibleText(text);
        log.debug("Selected '{}' from dropdown", text);
    }

    protected void selectByValue(WebElement element, String value) {
        Select select = new Select(element);
        select.selectByValue(value);
    }

    protected void selectByIndex(WebElement element, int index) {
        Select select = new Select(element);
        select.selectByIndex(index);
    }

    protected String getSelectedText(WebElement element) {
        Select select = new Select(element);
        return select.getFirstSelectedOption().getText();
    }

    // Get methods
    protected String getText(WebElement element) {
        return waitForElementClickable(element).getText().trim();
    }

    protected String getText(By locator) {
        return waitForElement(locator).getText().trim();
    }

    protected String getAttribute(WebElement element, String attribute) {
        return element.getAttribute(attribute);
    }

    protected String getValue(WebElement element) {
        return element.getAttribute("value");
    }

    // Verification methods
    protected boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    protected boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected boolean isEnabled(WebElement element) {
        return element.isEnabled();
    }

    protected boolean isSelected(WebElement element) {
        return element.isSelected();
    }

    // Table methods
    protected List<WebElement> getTableRows(WebElement table) {
        return table.findElements(By.tagName("tr"));
    }

    protected List<WebElement> getTableCells(WebElement row) {
        return row.findElements(By.tagName("td"));
    }

    protected String getCellText(WebElement table, int row, int col) {
        List<WebElement> rows = getTableRows(table);
        if (row < rows.size()) {
            List<WebElement> cells = getTableCells(rows.get(row));
            if (col < cells.size()) {
                return cells.get(col).getText();
            }
        }
        return null;
    }

    // Alert methods
    protected void acceptAlert() {
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
    }

    protected void dismissAlert() {
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().dismiss();
    }

    protected String getAlertText() {
        wait.until(ExpectedConditions.alertIsPresent());
        return driver.switchTo().alert().getText();
    }

    // Scroll methods
    protected void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    protected void scrollToTop() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
    }

    protected void scrollToBottom() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    // Frame methods
    protected void switchToFrame(WebElement frame) {
        driver.switchTo().frame(frame);
    }

    protected void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    // Wait for page load - with timeout handling
    protected void waitForPageLoad() {
        try {
            wait.until(driver -> ((JavascriptExecutor) driver)
                    .executeScript("return document.readyState").equals("complete"));
        } catch (Exception e) {
            log.warn("Page load wait timed out, continuing anyway: {}", e.getMessage());
            // Page may still be usable even if not fully loaded
        }
    }

    // Screenshot
    public byte[] takeScreenshot() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }
}