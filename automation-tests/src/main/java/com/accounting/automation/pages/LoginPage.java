package com.accounting.automation.pages;

import com.accounting.automation.config.ConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Login Page Object
 */
@Slf4j
public class LoginPage extends BasePage {

    @FindBy(id = "username")
    private WebElement usernameField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(css = "button[type='submit']")
    private WebElement loginButton;

    @FindBy(css = ".alert-danger")
    private WebElement errorMessage;

    @FindBy(css = ".alert-success")
    private WebElement successMessage;

    @FindBy(css = ".login-card")
    private WebElement loginCard;

    public LoginPage() {
        super();
    }

    public LoginPage open() {
        navigateTo(ConfigReader.getBaseUrl() + "/login");
        waitForPageLoad();
        log.info("Opened login page");
        return this;
    }

    public LoginPage enterUsername(String username) {
        waitForElementClickable(usernameField);
        type(usernameField, username);
        log.info("Entered username: {}", username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(passwordField, password);
        log.info("Entered password");
        return this;
    }

    public DashboardPage clickLoginButton() {
        try {
            // Try clicking the login button
            WebElement btn = driver.findElement(By.cssSelector("button[type='submit']"));
            if (btn.isDisplayed() && btn.isEnabled()) {
                btn.click();
            }
        } catch (Exception e) {
            log.warn("Login button click failed, trying JS click: {}", e.getMessage());
            try {
                WebElement btn = driver.findElement(By.cssSelector("button[type='submit']"));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            } catch (Exception e2) {
                log.error("JS click also failed: {}", e2.getMessage());
            }
        }
        waitForPageLoad();
        log.info("Clicked login button");
        return new DashboardPage();
    }

    public DashboardPage loginAs(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        return clickLoginButton();
    }

    public DashboardPage loginWithDefaultCredentials() {
        return loginAs(ConfigReader.getUsername(), ConfigReader.getPassword());
    }

    public boolean isErrorMessageDisplayed() {
        try {
            // Wait for page to reload after failed login
            waitForPageLoad();
            Thread.sleep(500);
            // Check for error message or error URL parameter
            String url = getCurrentUrl();
            if (url.contains("error")) {
                return true;
            }
            return driver.findElements(By.cssSelector(".alert-danger")).size() > 0
                   && driver.findElement(By.cssSelector(".alert-danger")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        try {
            return getText(errorMessage);
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isLoginPageDisplayed() {
        try {
            // Check if we're on login page by URL or form presence
            String url = getCurrentUrl();
            if (url.contains("/login")) {
                return true;
            }
            // Check for login card presence
            return driver.findElements(By.cssSelector(".login-card")).size() > 0
                   || driver.findElements(By.id("username")).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLoginButtonEnabled() {
        return isEnabled(loginButton);
    }
}