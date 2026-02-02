package com.accounting.automation.stepdefinitions;

import com.accounting.automation.config.ConfigReader;
import com.accounting.automation.pages.DashboardPage;
import com.accounting.automation.pages.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Login Step Definitions
 */
@Slf4j
public class LoginSteps {

    private LoginPage loginPage;
    private DashboardPage dashboardPage;

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        loginPage = new LoginPage().open();
        assertThat(loginPage.isLoginPageDisplayed())
                .as("Login page should be displayed")
                .isTrue();
    }

    @Given("I am logged in as admin")
    public void iAmLoggedInAsAdmin() {
        loginPage = new LoginPage().open();
        dashboardPage = loginPage.loginWithDefaultCredentials();
        assertThat(dashboardPage.isDashboardDisplayed())
                .as("Should be redirected to dashboard after login")
                .isTrue();
    }

    @Given("I am logged in with username {string} and password {string}")
    public void iAmLoggedInWithCredentials(String username, String password) {
        loginPage = new LoginPage().open();
        dashboardPage = loginPage.loginAs(username, password);
    }

    @When("I enter username {string}")
    public void iEnterUsername(String username) {
        loginPage.enterUsername(username);
    }

    @When("I enter password {string}")
    public void iEnterPassword(String password) {
        loginPage.enterPassword(password);
    }

    @When("I click the login button")
    public void iClickTheLoginButton() {
        dashboardPage = loginPage.clickLoginButton();
    }

    @When("I login with valid credentials")
    public void iLoginWithValidCredentials() {
        dashboardPage = loginPage.loginWithDefaultCredentials();
    }

    @When("I login with username {string} and password {string}")
    public void iLoginWithUsernameAndPassword(String username, String password) {
        dashboardPage = loginPage.loginAs(username, password);
    }

    @Then("I should be redirected to the dashboard")
    public void iShouldBeRedirectedToTheDashboard() {
        assertThat(dashboardPage.isDashboardDisplayed())
                .as("Dashboard should be displayed after successful login")
                .isTrue();
    }

    @Then("I should see an error message")
    public void iShouldSeeAnErrorMessage() {
        assertThat(loginPage.isErrorMessageDisplayed())
                .as("Error message should be displayed for invalid login")
                .isTrue();
    }

    @Then("I should see error message {string}")
    public void iShouldSeeErrorMessage(String expectedMessage) {
        assertThat(loginPage.getErrorMessage())
                .as("Error message should match")
                .contains(expectedMessage);
    }

    @Then("I should remain on the login page")
    public void iShouldRemainOnTheLoginPage() {
        assertThat(loginPage.isLoginPageDisplayed())
                .as("Should remain on login page after failed login")
                .isTrue();
    }
}