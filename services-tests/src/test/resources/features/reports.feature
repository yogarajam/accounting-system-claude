@reports @regression
Feature: Report Service API
  As an API consumer
  I want to generate financial reports through the API
  So that I can analyze the financial position

  Background:
    Given the API is available
    And I am authenticated as admin

  @smoke
  Scenario: Generate Trial Balance report
    When I request the Trial Balance as of today
    Then the response status code should be 200
    And the response should contain trial balance data
    And the total debits should equal total credits

  @smoke
  Scenario: Generate Profit and Loss report
    When I request the Profit and Loss for the current month
    Then the response status code should be 200
    And the response should contain revenue accounts
    And the response should contain expense accounts
    And the response should contain net income

  @smoke
  Scenario: Generate Balance Sheet report
    When I request the Balance Sheet as of today
    Then the response status code should be 200
    And the response should contain asset accounts
    And the response should contain liability accounts
    And the response should contain equity accounts
    And the balance sheet should be balanced

  Scenario: Generate General Ledger for specific account
    Given an account exists with code "1000"
    When I request the General Ledger for account "1000" for the current month
    Then the response status code should be 200
    And the response should contain ledger entries
    And the response should contain opening balance
    And the response should contain closing balance

  Scenario: Trial Balance with specific date
    When I request the Trial Balance as of "2024-01-31"
    Then the response status code should be 200
    And the as-of date should be "2024-01-31"

  Scenario: Profit and Loss with date range
    When I request the Profit and Loss from "2024-01-01" to "2024-01-31"
    Then the response status code should be 200
    And the start date should be "2024-01-01"
    And the end date should be "2024-01-31"

  Scenario: Balance Sheet historical date
    When I request the Balance Sheet as of "2024-01-31"
    Then the response status code should be 200
    And the as-of date should be "2024-01-31"

  Scenario: General Ledger with date range
    Given an account exists with code "1000"
    When I request the General Ledger for account "1000" from "2024-01-01" to "2024-01-31"
    Then the response status code should be 200
    And the start date should be "2024-01-01"
    And the end date should be "2024-01-31"

  @negative
  Scenario: General Ledger for non-existent account
    When I request the General Ledger for account ID 99999
    Then the response status code should be 404

  Scenario: Trial Balance shows all account types
    When I request the Trial Balance as of today
    Then the response should contain accounts of all types

  Scenario: Verify Trial Balance is always balanced
    When I request the Trial Balance as of today
    Then the total debits should equal total credits

  Scenario: Verify Balance Sheet equation
    When I request the Balance Sheet as of today
    Then total assets should equal total liabilities plus equity