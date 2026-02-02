@dashboard @smoke
Feature: Dashboard
  As a user
  I want to see the dashboard
  So that I can get an overview of the business

  Background:
    Given I am on the login page
    And I am logged in as admin

  Scenario: View dashboard after login
    Then I should be redirected to the dashboard

  Scenario: Navigate to Accounts from dashboard
    When I navigate to the accounts page
    Then I should see the accounts list

  Scenario: Navigate to Journal from dashboard
    When I navigate to the journal page
    Then I should see the journal entries list

  Scenario: Navigate to Reports from dashboard
    When I navigate to the reports page
    Then I should see the reports page