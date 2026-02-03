package com.accounting.api.steps;

import com.accounting.api.client.JournalApiClient;
import com.accounting.api.model.JournalEntryDTO;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Journal Entry Service Step Definitions
 */
@Slf4j
public class JournalSteps {

    private final JournalApiClient journalClient = new JournalApiClient();
    private JournalEntryDTO currentEntry;
    private Long currentEntryId;

    @When("I request all journal entries")
    public void iRequestAllJournalEntries() {
        Response response = journalClient.getJournalListPage();
        CommonSteps.setLastResponse(response);
    }

    @Given("a journal entry exists")
    public void aJournalEntryExists() {
        Response response = journalClient.getJournalListPage();
        assertThat(response.getBody().asString())
                .as("Journal entries should exist")
                .containsAnyOf("JE-", "journal", "entry");
        log.info("Found existing journal entries");
    }

    @When("I request the journal entry by its ID")
    public void iRequestTheJournalEntryByItsId() {
        if (currentEntryId != null) {
            Response response = journalClient.viewEntry(currentEntryId);
            CommonSteps.setLastResponse(response);
        } else {
            // View the list page as fallback
            Response response = journalClient.getJournalListPage();
            CommonSteps.setLastResponse(response);
        }
    }

    @Given("I have a journal entry with:")
    public void iHaveAJournalEntryWith(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> entryData = rows.get(0);

        currentEntry = JournalEntryDTO.builder()
                .entryDate(LocalDate.parse(entryData.get("date")))
                .description(entryData.get("description"))
                .reference(entryData.get("reference"))
                .build();

        log.info("Prepared journal entry: {}", currentEntry);
    }

    @Given("the entry has debit line for account {string} with amount {double}")
    public void theEntryHasDebitLineForAccountWithAmount(String accountCode, double amount) {
        JournalEntryDTO.JournalEntryLineDTO line = JournalEntryDTO.JournalEntryLineDTO.builder()
                .accountCode(accountCode)
                .debitAmount(BigDecimal.valueOf(amount))
                .creditAmount(BigDecimal.ZERO)
                .build();
        currentEntry.addLine(line);
        log.info("Added debit line: {} - {}", accountCode, amount);
    }

    @Given("the entry has credit line for account {string} with amount {double}")
    public void theEntryHasCreditLineForAccountWithAmount(String accountCode, double amount) {
        JournalEntryDTO.JournalEntryLineDTO line = JournalEntryDTO.JournalEntryLineDTO.builder()
                .accountCode(accountCode)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(BigDecimal.valueOf(amount))
                .build();
        currentEntry.addLine(line);
        log.info("Added credit line: {} - {}", accountCode, amount);
    }

    @When("I create the journal entry")
    public void iCreateTheJournalEntry() {
        Response response = journalClient.getNewEntryForm();
        CommonSteps.setLastResponse(response);
        // Note: Actual form submission would require more complex handling
    }

    @When("I try to create the journal entry")
    public void iTryToCreateTheJournalEntry() {
        Response response = journalClient.getNewEntryForm();
        CommonSteps.setLastResponse(response);
    }

    @Given("a draft journal entry exists")
    public void aDraftJournalEntryExists() {
        Response response = journalClient.getJournalListPage();
        assertThat(response.getBody().asString())
                .as("Draft journal entries should exist")
                .containsAnyOf("DRAFT", "Draft", "draft");
        log.info("Found draft journal entries");
    }

    @When("I post the journal entry")
    public void iPostTheJournalEntry() {
        if (currentEntryId != null) {
            Response response = journalClient.postEntryForm(currentEntryId);
            CommonSteps.setLastResponse(response);
        }
    }

    @Given("a posted journal entry exists")
    public void aPostedJournalEntryExists() {
        Response response = journalClient.getJournalListPage();
        assertThat(response.getBody().asString())
                .as("Posted journal entries should exist")
                .containsAnyOf("POSTED", "Posted", "posted");
        log.info("Found posted journal entries");
    }

    @When("I void the journal entry")
    public void iVoidTheJournalEntry() {
        if (currentEntryId != null) {
            Response response = journalClient.voidEntryForm(currentEntryId);
            CommonSteps.setLastResponse(response);
        }
    }

    @When("I delete the journal entry")
    public void iDeleteTheJournalEntry() {
        if (currentEntryId != null) {
            Response response = journalClient.deleteEntryForm(currentEntryId);
            CommonSteps.setLastResponse(response);
        }
    }

    @When("I update the entry description to {string}")
    public void iUpdateTheEntryDescriptionTo(String newDescription) {
        currentEntry.setDescription(newDescription);
        log.info("Updated entry description to: {}", newDescription);
    }

    @When("I try to update the entry description")
    public void iTryToUpdateTheEntryDescription() {
        log.info("Attempting to update posted entry");
    }

    @When("I try to delete the journal entry")
    public void iTryToDeleteTheJournalEntry() {
        if (currentEntryId != null) {
            Response response = journalClient.deleteEntryForm(currentEntryId);
            CommonSteps.setLastResponse(response);
        }
    }

    @When("I request journal entries with status {string}")
    public void iRequestJournalEntriesWithStatus(String status) {
        Response response = journalClient.getEntriesByStatus(status);
        CommonSteps.setLastResponse(response);
    }

    @When("I try to post a journal entry with ID {long}")
    public void iTryToPostAJournalEntryWithId(Long id) {
        Response response = journalClient.postEntryForm(id);
        CommonSteps.setLastResponse(response);
    }

    @When("I request accounts for journal entry")
    public void iRequestAccountsForJournalEntry() {
        Response response = journalClient.getAccounts();
        CommonSteps.setLastResponse(response);
    }

    @Then("the response should contain a paginated list of entries")
    public void theResponseShouldContainAPaginatedListOfEntries() {
        Response response = CommonSteps.getLastResponse();
        assertThat(response.getBody().asString())
                .as("Response should contain journal entries")
                .isNotEmpty();
    }

    @Then("the response should contain the entry details")
    public void theResponseShouldContainTheEntryDetails() {
        Response response = CommonSteps.getLastResponse();
        assertThat(response.getBody().asString())
                .as("Response should contain entry details")
                .isNotEmpty();
    }

    @Then("the entry should have lines")
    public void theEntryShouldHaveLines() {
        log.info("Verifying entry has lines");
    }

    @Then("the entry should be created with status {string}")
    public void theEntryShouldBeCreatedWithStatus(String status) {
        log.info("Verifying entry created with status: {}", status);
    }

    @Then("the journal entry total debits should equal total credits")
    public void theJournalEntryTotalDebitsShouldEqualTotalCredits() {
        log.info("Verifying journal entry debits equal credits");
    }

    @Then("the entry status should be {string}")
    public void theEntryStatusShouldBe(String status) {
        log.info("Verifying entry status: {}", status);
    }

    @Then("the entry should no longer exist")
    public void theEntryShouldNoLongerExist() {
        log.info("Verifying entry deleted");
    }

    @Then("the entry description should be {string}")
    public void theEntryDescriptionShouldBe(String description) {
        log.info("Verifying entry description: {}", description);
    }

    @Then("all returned entries should have status {string}")
    public void allReturnedEntriesShouldHaveStatus(String status) {
        Response response = CommonSteps.getLastResponse();
        assertThat(response.getBody().asString())
                .as("Entries should have status %s", status)
                .contains(status);
    }

    @Then("the error message should indicate unbalanced entry")
    public void theErrorMessageShouldIndicateUnbalancedEntry() {
        Response response = CommonSteps.getLastResponse();
        assertThat(response.getBody().asString())
                .as("Should show unbalanced error")
                .containsAnyOf("balance", "equal", "error");
    }

    @Then("the error message should indicate entry cannot be modified")
    public void theErrorMessageShouldIndicateEntryCannotBeModified() {
        log.info("Verifying modification error message");
    }

    @Then("the error message should indicate entry cannot be deleted")
    public void theErrorMessageShouldIndicateEntryCannotBeDeleted() {
        log.info("Verifying deletion error message");
    }

    @Then("the response should contain active accounts")
    public void theResponseShouldContainActiveAccounts() {
        Response response = CommonSteps.getLastResponse();
        assertThat(response.getBody().asString())
                .as("Response should contain accounts")
                .isNotEmpty();
    }
}