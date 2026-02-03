@accounts @regression
Feature: Chart of Accounts Management
  As an accountant
  I want to manage the chart of accounts
  So that I can organize financial transactions properly

  Background:
    Given I am on the login page
    And I am logged in as admin
    And I am on the accounts page

  @smoke
  Scenario: View chart of accounts
    Then I should see the accounts list
    And the account count should be at least 1

  @wip
  Scenario: Create a new asset account
    When I click on New Account button
    And I enter account code "1500"
    And I enter account name "Office Equipment"
    And I select account type "Asset"
    And I enter description "Office equipment and furniture"
    And I click the save button
    Then I should see a success message
    And I should see the account "1500" in the list

  @wip
  Scenario: Create a new liability account
    When I create a new account with code "2100", name "Short-term Loans", type "Liability"
    Then I should see a success message
    And I should see the account "2100" in the list

  @wip
  Scenario: Create a new revenue account
    When I create a new account with code "4100", name "Consulting Revenue", type "Revenue"
    Then I should see a success message
    And I should see the account "4100" in the list

  @wip
  Scenario: Create a new expense account
    When I create a new account with code "5100", name "Office Supplies Expense", type "Expense"
    Then I should see a success message
    And I should see the account "5100" in the list

  @wip
  Scenario: Create account with random data
    When I create a new account with random data
    Then I should see a success message
    And I should see the newly created account in the list

  Scenario: Filter accounts by Asset type
    When I filter accounts by type "Assets"
    Then I should see the accounts list

  Scenario: Filter accounts by Liability type
    When I filter accounts by type "Liabilities"
    Then I should see the accounts list

  Scenario: Filter accounts by Revenue type
    When I filter accounts by type "Revenue"
    Then I should see the accounts list

  Scenario: Filter accounts by Expense type
    When I filter accounts by type "Expenses"
    Then I should see the accounts list

  @negative @wip
  Scenario: Create account with duplicate code
    When I create a new account with code "1000", name "Duplicate Cash", type "Asset"
    Then I should see an error message on accounts page

  @wip
  Scenario Outline: Create various account types
    When I create a new account with code "<code>", name "<name>", type "<type>"
    Then I should see a success message
    And I should see the account "<code>" in the list

    Examples:
      | code | name                | type      |
      | 1600 | Prepaid Insurance   | Asset     |
      | 2200 | Accrued Expenses    | Liability |
      | 3100 | Owner's Capital     | Equity    |
      | 4200 | Service Revenue     | Revenue   |
      | 5200 | Rent Expense        | Expense   |