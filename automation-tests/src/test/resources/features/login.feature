@login @smoke
Feature: Login Functionality
  As a user
  I want to login to the accounting system
  So that I can access my financial data

  Background:
    Given I am on the login page

  @positive
  Scenario: Successful login with valid credentials
    When I enter username "admin"
    And I enter password "admin"
    And I click the login button
    Then I should be redirected to the dashboard

  @positive
  Scenario: Login using default credentials
    When I login with valid credentials
    Then I should be redirected to the dashboard

  @negative
  Scenario: Login with invalid username
    When I login with username "invaliduser" and password "admin"
    Then I should see an error message
    And I should remain on the login page

  @negative
  Scenario: Login with invalid password
    When I login with username "admin" and password "wrongpassword"
    Then I should see an error message
    And I should remain on the login page

  @negative
  Scenario: Login with empty credentials
    When I click the login button
    Then I should remain on the login page

  @negative
  Scenario Outline: Login with various invalid credentials
    When I login with username "<username>" and password "<password>"
    Then I should see an error message
    And I should remain on the login page

    Examples:
      | username    | password      |
      | admin       | wrongpass     |
      | wronguser   | admin         |
      | wronguser   | wrongpass     |
      |             | admin         |
      | admin       |               |