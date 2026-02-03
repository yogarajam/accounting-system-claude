@journal @regression
Feature: Journal Entry Service API
  As an API consumer
  I want to manage journal entries through the API
  So that I can record financial transactions

  Background:
    Given the API is available
    And I am authenticated as admin

  @smoke
  Scenario: Get all journal entries
    When I request all journal entries
    Then the response status code should be 200
    And the response should contain a paginated list of entries

  @smoke
  Scenario: Get journal entry by ID
    Given a journal entry exists
    When I request the journal entry by its ID
    Then the response status code should be 200
    And the response should contain the entry details
    And the entry should have lines

  Scenario: Create a balanced journal entry
    Given I have a journal entry with:
      | date       | description        | reference |
      | 2024-01-15 | Test journal entry | TEST-001  |
    And the entry has debit line for account "1000" with amount 1000.00
    And the entry has credit line for account "4000" with amount 1000.00
    When I create the journal entry
    Then the response status code should be 201
    And the entry should be created with status "DRAFT"
    And the journal entry total debits should equal total credits

  Scenario: Post a draft journal entry
    Given a draft journal entry exists
    When I post the journal entry
    Then the response status code should be 200
    And the entry status should be "POSTED"

  Scenario: Void a posted journal entry
    Given a posted journal entry exists
    When I void the journal entry
    Then the response status code should be 200
    And the entry status should be "VOIDED"

  Scenario: Delete a draft journal entry
    Given a draft journal entry exists
    When I delete the journal entry
    Then the response status code should be 200
    And the entry should no longer exist

  Scenario: Update a draft journal entry
    Given a draft journal entry exists
    When I update the entry description to "Updated description"
    Then the response status code should be 200
    And the entry description should be "Updated description"

  Scenario: Get journal entries by status
    When I request journal entries with status "POSTED"
    Then the response status code should be 200
    And all returned entries should have status "POSTED"

  @negative
  Scenario: Create unbalanced journal entry
    Given I have a journal entry with:
      | date       | description          | reference |
      | 2024-01-15 | Unbalanced entry     | TEST-002  |
    And the entry has debit line for account "1000" with amount 1000.00
    And the entry has credit line for account "4000" with amount 500.00
    When I try to create the journal entry
    Then the response status code should be 400
    And the error message should indicate unbalanced entry

  @negative
  Scenario: Post a non-existent journal entry
    When I try to post a journal entry with ID 99999
    Then the response status code should be 404

  @negative
  Scenario: Update a posted journal entry
    Given a posted journal entry exists
    When I try to update the entry description
    Then the response status code should be 400
    And the error message should indicate entry cannot be modified

  @negative
  Scenario: Delete a posted journal entry
    Given a posted journal entry exists
    When I try to delete the journal entry
    Then the response status code should be 400
    And the error message should indicate entry cannot be deleted

  Scenario: Get available accounts for journal entry
    When I request accounts for journal entry
    Then the response status code should be 200
    And the response should contain active accounts