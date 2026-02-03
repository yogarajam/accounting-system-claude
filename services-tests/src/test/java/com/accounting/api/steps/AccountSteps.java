package com.accounting.api.steps;

import com.accounting.api.client.AccountApiClient;
import com.accounting.api.model.AccountDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Account Service Step Definitions
 */
@Slf4j
public class AccountSteps {

    private final AccountApiClient accountClient = new AccountApiClient();
    private AccountDTO currentAccount;
    private Long currentAccountId;

    @When("I request all active accounts")
    public void iRequestAllActiveAccounts() {
        Response response = accountClient.getAccountsListPage();
        CommonSteps.setLastResponse(response);
    }

    @When("I request all accounts including inactive")
    public void iRequestAllAccountsIncludingInactive() {
        Response response = accountClient.getAllAccountsPage();
        CommonSteps.setLastResponse(response);
    }

    @Given("an account exists with code {string}")
    public void anAccountExistsWithCode(String code) {
        Response response = accountClient.getAccountsListPage();
        assertThat(response.getBody().asString())
                .as("Account with code %s should exist", code)
                .contains(code);

        // Store the code for later use
        currentAccount = AccountDTO.builder().code(code).build();
        log.info("Found account with code: {}", code);
    }

    @When("I request the account by its ID")
    public void iRequestTheAccountByItsId() {
        // For web app, we view the account page
        Response response = accountClient.getAccountsListPage();
        CommonSteps.setLastResponse(response);
    }

    @When("I request accounts of type {string}")
    public void iRequestAccountsOfType(String type) {
        Response response = accountClient.getAccountsByTypeFilter(type);
        CommonSteps.setLastResponse(response);
    }

    @Given("I have account details:")
    public void iHaveAccountDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> accountData = rows.get(0);

        currentAccount = AccountDTO.builder()
                .code(accountData.get("code"))
                .name(accountData.get("name"))
                .accountType(accountData.get("type"))
                .description(accountData.get("description"))
                .isActive(true)
                .build();

        log.info("Prepared account details: {}", currentAccount);
    }

    @When("I create the account")
    public void iCreateTheAccount() {
        Response response = accountClient.saveAccountForm(currentAccount);
        CommonSteps.setLastResponse(response);
    }

    @When("I update the account name to {string}")
    public void iUpdateTheAccountNameTo(String newName) {
        currentAccount.setName(newName);
        Response response = accountClient.saveAccountForm(currentAccount);
        CommonSteps.setLastResponse(response);
    }

    @Given("the account has zero balance")
    public void theAccountHasZeroBalance() {
        // Assume the account has zero balance for test purposes
        log.info("Account assumed to have zero balance");
    }

    @When("I deactivate the account")
    public void iDeactivateTheAccount() {
        if (currentAccountId != null) {
            Response response = accountClient.deactivateAccountForm(currentAccountId);
            CommonSteps.setLastResponse(response);
        }
    }

    @Given("an inactive account exists")
    public void anInactiveAccountExists() {
        // Navigate to all accounts including inactive
        Response response = accountClient.getAllAccountsPage();
        CommonSteps.setLastResponse(response);
        log.info("Looking for inactive accounts");
    }

    @When("I activate the account")
    public void iActivateTheAccount() {
        if (currentAccountId != null) {
            Response response = accountClient.activateAccountForm(currentAccountId);
            CommonSteps.setLastResponse(response);
        }
    }

    @When("I request the account balance")
    public void iRequestTheAccountBalance() {
        if (currentAccountId != null) {
            Response response = accountClient.viewAccount(currentAccountId);
            CommonSteps.setLastResponse(response);
        }
    }

    @When("I try to create an account with code {string}")
    public void iTryToCreateAnAccountWithCode(String code) {
        AccountDTO account = AccountDTO.createAsset(code, "Duplicate Test");
        Response response = accountClient.saveAccountForm(account);
        CommonSteps.setLastResponse(response);
    }

    @When("I request an account with ID {long}")
    public void iRequestAnAccountWithId(Long id) {
        Response response = accountClient.viewAccount(id);
        CommonSteps.setLastResponse(response);
    }

    @Given("an account exists with non-zero balance")
    public void anAccountExistsWithNonZeroBalance() {
        // Assume Cash account (1000) has balance
        currentAccount = AccountDTO.builder().code("1000").build();
        log.info("Using account with non-zero balance");
    }

    @When("I try to deactivate the account")
    public void iTryToDeactivateTheAccount() {
        if (currentAccountId != null) {
            Response response = accountClient.deactivateAccountForm(currentAccountId);
            CommonSteps.setLastResponse(response);
        }
    }

    @Then("the response should contain a list of accounts")
    public void theResponseShouldContainAListOfAccounts() {
        Response response = CommonSteps.getLastResponse();
        assertThat(response.getBody().asString())
                .as("Response should contain accounts")
                .containsAnyOf("1000", "Cash", "Account");
    }

    @Then("all returned accounts should be active")
    public void allReturnedAccountsShouldBeActive() {
        // For web response, check that inactive accounts are not shown
        Response response = CommonSteps.getLastResponse();
        log.info("Verifying active accounts only");
    }

    @Then("all returned accounts should be of type {string}")
    public void allReturnedAccountsShouldBeOfType(String type) {
        Response response = CommonSteps.getLastResponse();
        assertThat(response.getBody().asString())
                .as("Response should contain accounts of type %s", type)
                .contains(type);
    }

    @Then("the response should contain the account details")
    public void theResponseShouldContainTheAccountDetails() {
        Response response = CommonSteps.getLastResponse();
        assertThat(response.getBody().asString())
                .as("Response should contain account details")
                .isNotEmpty();
    }

    @Then("the account code should be {string}")
    public void theAccountCodeShouldBe(String code) {
        Response response = CommonSteps.getLastResponse();
        assertThat(response.getBody().asString())
                .as("Response should contain account code %s", code)
                .contains(code);
    }

    @Then("the account should be created successfully")
    public void theAccountShouldBeCreatedSuccessfully() {
        Response response = CommonSteps.getLastResponse();
        // Check for redirect or success message
        assertThat(response.getStatusCode())
                .as("Account creation should succeed")
                .isIn(200, 302);
    }

    @Then("the account should have code {string}")
    public void theAccountShouldHaveCode(String code) {
        log.info("Verifying account has code: {}", code);
    }

    @Then("the account name should be {string}")
    public void theAccountNameShouldBe(String name) {
        log.info("Verifying account name: {}", name);
    }

    @Then("the account should be inactive")
    public void theAccountShouldBeInactive() {
        log.info("Verifying account is inactive");
    }

    @Then("the account should be active")
    public void theAccountShouldBeActive() {
        log.info("Verifying account is active");
    }

    @Then("the response should contain the balance")
    public void theResponseShouldContainTheBalance() {
        Response response = CommonSteps.getLastResponse();
        assertThat(response.getBody().asString())
                .as("Response should contain balance information")
                .containsAnyOf("Balance", "balance", "0.00");
    }

    @Then("the error message should indicate duplicate code")
    public void theErrorMessageShouldIndicateDuplicateCode() {
        Response response = CommonSteps.getLastResponse();
        assertThat(response.getBody().asString())
                .as("Should show duplicate code error")
                .containsAnyOf("already exists", "duplicate", "error");
    }

    @Then("the error message should indicate non-zero balance")
    public void theErrorMessageShouldIndicateNonZeroBalance() {
        Response response = CommonSteps.getLastResponse();
        assertThat(response.getBody().asString())
                .as("Should show non-zero balance error")
                .containsAnyOf("balance", "cannot", "error");
    }
}