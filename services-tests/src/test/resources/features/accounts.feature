@accounts @regression
Feature: Account Service API
  As an API consumer
  I want to manage accounts through the API
  So that I can maintain the chart of accounts

  Background:
    Given the API is available
    And I am authenticated as admin

  @smoke
  Scenario: Get all active accounts
    When I request all active accounts
    Then the response status code should be 200
    And the response should contain a list of accounts
    And all returned accounts should be active

  @smoke
  Scenario: Get all accounts including inactive
    When I request all accounts including inactive
    Then the response status code should be 200
    And the response should contain a list of accounts

  Scenario: Get account by ID
    Given an account exists with code "1000"
    When I request the account by its ID
    Then the response status code should be 200
    And the response should contain the account details
    And the account code should be "1000"

  Scenario: Get accounts by type
    When I request accounts of type "ASSET"
    Then the response status code should be 200
    And the response should contain a list of accounts
    And all returned accounts should be of type "ASSET"

  Scenario: Create a new account
    Given I have account details:
      | code | name           | type  | description      |
      | 9001 | Test Account   | ASSET | Test description |
    When I create the account
    Then the response status code should be 201
    And the account should be created successfully
    And the account should have code "9001"

  Scenario: Update an existing account
    Given an account exists with code "9001"
    When I update the account name to "Updated Test Account"
    Then the response status code should be 200
    And the account name should be "Updated Test Account"

  Scenario: Deactivate an account with zero balance
    Given an account exists with code "9001"
    And the account has zero balance
    When I deactivate the account
    Then the response status code should be 200
    And the account should be inactive

  Scenario: Activate a deactivated account
    Given an inactive account exists
    When I activate the account
    Then the response status code should be 200
    And the account should be active

  Scenario: Get account balance
    Given an account exists with code "1000"
    When I request the account balance
    Then the response status code should be 200
    And the response should contain the balance

  @negative
  Scenario: Create account with duplicate code
    Given an account exists with code "1000"
    When I try to create an account with code "1000"
    Then the response status code should be 400
    And the error message should indicate duplicate code

  @negative
  Scenario: Get non-existent account
    When I request an account with ID 99999
    Then the response status code should be 404

  @negative
  Scenario: Deactivate account with non-zero balance
    Given an account exists with non-zero balance
    When I try to deactivate the account
    Then the response status code should be 400
    And the error message should indicate non-zero balance

  Scenario Outline: Filter accounts by type
    When I request accounts of type "<type>"
    Then the response status code should be 200
    And all returned accounts should be of type "<type>"

    Examples:
      | type      |
      | ASSET     |
      | LIABILITY |
      | EQUITY    |
      | REVENUE   |
      | EXPENSE   |