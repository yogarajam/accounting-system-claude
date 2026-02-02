@reports @regression
Feature: Financial Reports
  As a business owner
  I want to generate financial reports
  So that I can analyze the business performance

  Background:
    Given I am on the login page
    And I am logged in as admin
    And I am on the reports page

  @smoke
  Scenario: Generate Trial Balance for current month
    When I generate Trial Balance for current month
    Then I should see the report content
    And the Trial Balance should be balanced

  Scenario: Generate Trial Balance with custom date range
    When I open the Trial Balance report
    And I generate Trial Balance from "2024-01-01" to "2024-12-31"
    Then I should see the report content
    And I should see total debits equal to total credits

  @smoke
  Scenario: Generate Profit and Loss for current month
    When I generate Profit and Loss for current month
    Then I should see the report content
    And I should see the net income

  Scenario: Generate Profit and Loss with custom date range
    When I generate Profit and Loss from "2024-01-01" to "2024-12-31"
    Then I should see the report content

  @smoke
  Scenario: Generate Balance Sheet as of today
    When I generate Balance Sheet as of today
    Then I should see the report content
    And I should see total assets and liabilities

  Scenario: Generate Balance Sheet with specific date
    When I generate Balance Sheet as of "2024-12-31"
    Then I should see the report content

  Scenario: Open General Ledger report
    When I open the General Ledger report
    Then I should see the report content

  Scenario Outline: Generate various reports
    When I open the <report> report
    Then I should see the report content

    Examples:
      | report          |
      | Trial Balance   |
      | Profit and Loss |
      | Balance Sheet   |
      | General Ledger  |