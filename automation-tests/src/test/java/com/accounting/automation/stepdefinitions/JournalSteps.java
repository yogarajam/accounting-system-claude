package com.accounting.automation.stepdefinitions;

import com.accounting.automation.pages.DashboardPage;
import com.accounting.automation.pages.JournalPage;
import com.accounting.automation.utils.TestDataGenerator;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Journal Entry Step Definitions
 */
@Slf4j
public class JournalSteps {

    private DashboardPage dashboardPage;
    private JournalPage journalPage;
    private String lastCreatedReference;

    @Given("I am on the journal page")
    public void iAmOnTheJournalPage() {
        dashboardPage = new DashboardPage();
        journalPage = dashboardPage.navigateToJournal();
        assertThat(journalPage.isJournalPageDisplayed())
                .as("Journal page should be displayed")
                .isTrue();
    }

    @When("I navigate to the journal page")
    public void iNavigateToTheJournalPage() {
        dashboardPage = new DashboardPage();
        journalPage = dashboardPage.navigateToJournal();
    }

    @When("I click on New Entry button")
    public void iClickOnNewEntryButton() {
        journalPage.clickNewEntryButton();
    }

    @When("I enter entry date {string}")
    public void iEnterEntryDate(String date) {
        journalPage.enterEntryDate(date);
    }

    @When("I enter today as entry date")
    public void iEnterTodayAsEntryDate() {
        journalPage.enterEntryDate(TestDataGenerator.getTodayDate());
    }

    @When("I enter reference {string}")
    public void iEnterReference(String reference) {
        journalPage.enterReference(reference);
        lastCreatedReference = reference;
    }

    @When("I enter a random reference")
    public void iEnterARandomReference() {
        lastCreatedReference = TestDataGenerator.generateReference();
        journalPage.enterReference(lastCreatedReference);
    }

    @When("I enter journal description {string}")
    public void iEnterJournalDescription(String description) {
        journalPage.enterDescription(description);
    }

    @When("I select debit account {string} with amount {string}")
    public void iSelectDebitAccountWithAmount(String account, String amount) {
        journalPage.selectDebitAccount(0, account);
        journalPage.enterDebitAmount(0, amount);
    }

    @When("I select credit account {string} with amount {string}")
    public void iSelectCreditAccountWithAmount(String account, String amount) {
        journalPage.selectCreditAccount(0, account);
        journalPage.enterCreditAmount(0, amount);
    }

    @When("I create a simple journal entry with debit {string} and credit {string} for amount {string}")
    public void iCreateSimpleJournalEntry(String debitAccount, String creditAccount, String amount) {
        lastCreatedReference = TestDataGenerator.generateReference();
        journalPage.createSimpleEntry(
                TestDataGenerator.getTodayDate(),
                lastCreatedReference,
                "Test journal entry",
                debitAccount,
                creditAccount,
                amount
        );
    }

    @When("I save the journal entry")
    public void iSaveTheJournalEntry() {
        journalPage.clickSaveButton();
    }

    @When("I view journal entry {string}")
    public void iViewJournalEntry(String reference) {
        journalPage.viewEntry(reference);
    }

    @When("I post journal entry {string}")
    public void iPostJournalEntry(String reference) {
        journalPage.postEntry(reference);
    }

    @Then("I should see the journal entries list")
    public void iShouldSeeTheJournalEntriesList() {
        assertThat(journalPage.isJournalPageDisplayed())
                .as("Journal entries list should be displayed")
                .isTrue();
    }

    @Then("I should see the journal entry {string} in the list")
    public void iShouldSeeTheJournalEntryInTheList(String reference) {
        assertThat(journalPage.isEntryDisplayed(reference))
                .as("Journal entry %s should be displayed", reference)
                .isTrue();
    }

    @Then("I should see the newly created journal entry in the list")
    public void iShouldSeeTheNewlyCreatedJournalEntryInTheList() {
        assertThat(journalPage.isEntryDisplayed(lastCreatedReference))
                .as("Newly created journal entry should be displayed")
                .isTrue();
    }

    @Then("I should see a success message on journal page")
    public void iShouldSeeASuccessMessageOnJournalPage() {
        assertThat(journalPage.isSuccessMessageDisplayed())
                .as("Success message should be displayed")
                .isTrue();
    }

    @Then("the journal entry count should be at least {int}")
    public void theJournalEntryCountShouldBeAtLeast(int minCount) {
        assertThat(journalPage.getEntryCount())
                .as("Journal entry count should be at least %d", minCount)
                .isGreaterThanOrEqualTo(minCount);
    }
}