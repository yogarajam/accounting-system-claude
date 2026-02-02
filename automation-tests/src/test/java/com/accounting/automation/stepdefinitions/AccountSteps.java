package com.accounting.automation.stepdefinitions;

import com.accounting.automation.pages.AccountsPage;
import com.accounting.automation.pages.DashboardPage;
import com.accounting.automation.utils.TestDataGenerator;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Account Step Definitions
 */
@Slf4j
public class AccountSteps {

    private DashboardPage dashboardPage;
    private AccountsPage accountsPage;
    private String lastCreatedAccountCode;

    @Given("I am on the accounts page")
    public void iAmOnTheAccountsPage() {
        dashboardPage = new DashboardPage();
        accountsPage = dashboardPage.navigateToAccounts();
        assertThat(accountsPage.isAccountsPageDisplayed())
                .as("Accounts page should be displayed")
                .isTrue();
    }

    @When("I navigate to the accounts page")
    public void iNavigateToTheAccountsPage() {
        dashboardPage = new DashboardPage();
        accountsPage = dashboardPage.navigateToAccounts();
    }

    @When("I click on New Account button")
    public void iClickOnNewAccountButton() {
        accountsPage.clickNewAccountButton();
    }

    @When("I enter account code {string}")
    public void iEnterAccountCode(String code) {
        accountsPage.enterAccountCode(code);
        lastCreatedAccountCode = code;
    }

    @When("I enter account name {string}")
    public void iEnterAccountName(String name) {
        accountsPage.enterAccountName(name);
    }

    @When("I select account type {string}")
    public void iSelectAccountType(String type) {
        accountsPage.selectAccountType(type);
    }

    @When("I enter description {string}")
    public void iEnterDescription(String description) {
        accountsPage.enterDescription(description);
    }

    @When("I click the save button")
    public void iClickTheSaveButton() {
        accountsPage.clickSaveButton();
    }

    @When("I create a new account with code {string}, name {string}, type {string}")
    public void iCreateNewAccount(String code, String name, String type) {
        accountsPage.createAccount(code, name, type, "Test account");
        lastCreatedAccountCode = code;
    }

    @When("I create a new account with random data")
    public void iCreateNewAccountWithRandomData() {
        String code = TestDataGenerator.generateAccountCode();
        String name = TestDataGenerator.generateAccountName();
        accountsPage.createAccount(code, name, "Asset", "Generated test account");
        lastCreatedAccountCode = code;
    }

    @When("I filter accounts by type {string}")
    public void iFilterAccountsByType(String type) {
        switch (type.toLowerCase()) {
            case "asset", "assets" -> accountsPage.filterByAssets();
            case "liability", "liabilities" -> accountsPage.filterByLiabilities();
            case "equity" -> accountsPage.filterByEquity();
            case "revenue" -> accountsPage.filterByRevenue();
            case "expense", "expenses" -> accountsPage.filterByExpenses();
            default -> accountsPage.filterByAll();
        }
    }

    @When("I edit account with code {string}")
    public void iEditAccountWithCode(String code) {
        accountsPage.clickEditAccount(code);
    }

    @When("I view account with code {string}")
    public void iViewAccountWithCode(String code) {
        accountsPage.clickViewAccount(code);
    }

    @Then("I should see the accounts list")
    public void iShouldSeeTheAccountsList() {
        assertThat(accountsPage.isAccountsPageDisplayed())
                .as("Accounts list should be displayed")
                .isTrue();
    }

    @Then("I should see the account {string} in the list")
    public void iShouldSeeTheAccountInTheList(String accountCode) {
        assertThat(accountsPage.isAccountDisplayed(accountCode))
                .as("Account %s should be displayed in the list", accountCode)
                .isTrue();
    }

    @Then("I should see the newly created account in the list")
    public void iShouldSeeTheNewlyCreatedAccountInTheList() {
        assertThat(accountsPage.isAccountDisplayed(lastCreatedAccountCode))
                .as("Newly created account should be displayed")
                .isTrue();
    }

    @Then("I should see a success message")
    public void iShouldSeeASuccessMessage() {
        assertThat(accountsPage.isSuccessMessageDisplayed())
                .as("Success message should be displayed")
                .isTrue();
    }

    @Then("I should see success message containing {string}")
    public void iShouldSeeSuccessMessageContaining(String message) {
        assertThat(accountsPage.getSuccessMessage())
                .as("Success message should contain: %s", message)
                .containsIgnoringCase(message);
    }

    @Then("I should see an error message on accounts page")
    public void iShouldSeeAnErrorMessageOnAccountsPage() {
        assertThat(accountsPage.isErrorMessageDisplayed())
                .as("Error message should be displayed")
                .isTrue();
    }

    @Then("the account count should be at least {int}")
    public void theAccountCountShouldBeAtLeast(int minCount) {
        assertThat(accountsPage.getAccountCount())
                .as("Account count should be at least %d", minCount)
                .isGreaterThanOrEqualTo(minCount);
    }
}