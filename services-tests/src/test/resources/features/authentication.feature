@authentication @regression @smoke
Feature: Authentication Service
  As an API consumer
  I want to authenticate with the system
  So that I can access protected resources

  Scenario: Successful login with valid credentials
    Given I am not authenticated
    When I login with username "admin" and password "admin123"
    Then the login should be successful
    And I should receive a session cookie
    And I should be redirected to dashboard

  Scenario: Access dashboard after login
    Given I am logged in as admin
    When I access the dashboard
    Then the response status code should be 200
    And the response should contain dashboard content

  Scenario: Logout successfully
    Given I am logged in as admin
    When I logout
    Then the logout should be successful
    And the session should be invalidated

  @negative
  Scenario: Login with invalid password
    Given I am not authenticated
    When I login with username "admin" and password "wrongpassword"
    Then the login should fail
    And I should remain on the login page
    And an error message should be displayed

  @negative
  Scenario: Login with invalid username
    Given I am not authenticated
    When I login with username "invaliduser" and password "admin123"
    Then the login should fail
    And I should remain on the login page

  @negative
  Scenario: Access protected resource without authentication
    Given I am not authenticated
    When I try to access "/dashboard" without authentication
    Then I should be redirected to login page

  @negative
  Scenario: Access protected API without authentication
    Given I am not authenticated
    When I try to access "/accounts" without authentication
    Then I should be redirected to login page

  Scenario: Session persistence
    Given I am logged in as admin
    When I make multiple requests
    Then the session should remain valid
    And all requests should succeed

  Scenario: Access accounts page after login
    Given I am logged in as admin
    When I access the accounts page
    Then the response status code should be 200

  Scenario: Access journal page after login
    Given I am logged in as admin
    When I access the journal page
    Then the response status code should be 200

  Scenario: Access reports page after login
    Given I am logged in as admin
    When I access the reports page
    Then the response status code should be 200

  Scenario: Access invoices page after login
    Given I am logged in as admin
    When I access the invoices page
    Then the response status code should be 200