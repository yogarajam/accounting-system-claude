@journal @regression
Feature: Journal Entry Management
  As an accountant
  I want to create and manage journal entries
  So that I can record financial transactions

  Background:
    Given I am on the login page
    And I am logged in as admin
    And I am on the journal page

  @smoke
  Scenario: View journal entries list
    Then I should see the journal entries list

  @smoke
  Scenario: Create a simple journal entry
    When I click on New Entry button
    And I enter today as entry date
    And I enter a random reference
    And I enter journal description "Test journal entry"
    And I select debit account "1000 - Cash" with amount "1000.00"
    And I select credit account "4000 - Sales Revenue" with amount "1000.00"
    And I save the journal entry
    Then I should see a success message on journal page
    And I should see the newly created journal entry in the list

  Scenario: Create journal entry for cash sale
    When I create a simple journal entry with debit "1000 - Cash" and credit "4000 - Sales Revenue" for amount "500.00"
    Then I should see a success message on journal page
    And I should see the newly created journal entry in the list

  Scenario: Create journal entry for expense payment
    When I create a simple journal entry with debit "5000 - Rent Expense" and credit "1000 - Cash" for amount "1500.00"
    Then I should see a success message on journal page
    And I should see the newly created journal entry in the list

  Scenario: Create journal entry for bank deposit
    When I create a simple journal entry with debit "1010 - Bank" and credit "1000 - Cash" for amount "2000.00"
    Then I should see a success message on journal page

  Scenario: View a journal entry
    Given I create a simple journal entry with debit "1000 - Cash" and credit "4000 - Sales Revenue" for amount "100.00"
    When I view journal entry with the created reference
    Then I should see the journal entry details

  @wip
  Scenario: Post a journal entry
    Given I create a simple journal entry with debit "1000 - Cash" and credit "4000 - Sales Revenue" for amount "750.00"
    When I post journal entry with the created reference
    Then I should see a success message on journal page

  Scenario Outline: Create various journal entries
    When I create a simple journal entry with debit "<debit_account>" and credit "<credit_account>" for amount "<amount>"
    Then I should see a success message on journal page

    Examples:
      | debit_account        | credit_account       | amount   |
      | 1000 - Cash          | 4000 - Sales Revenue | 1000.00  |
      | 5000 - Rent Expense  | 1000 - Cash          | 800.00   |
      | 1010 - Bank          | 1000 - Cash          | 5000.00  |
      | 5100 - Utilities     | 2000 - Accounts Payable | 250.00 |