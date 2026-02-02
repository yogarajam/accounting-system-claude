package com.accounting.automation.pages;

import com.accounting.automation.config.ConfigReader;
import lombok.extern.slf4j.Slf4j;
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
        click(loginButton);
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
        return isDisplayed(errorMessage);
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }

    public boolean isLoginPageDisplayed() {
        return isDisplayed(usernameField) && isDisplayed(passwordField);
    }

    public boolean isLoginButtonEnabled() {
        return isEnabled(loginButton);
    }
}