@invoices @regression
Feature: Invoice Service API
  As an API consumer
  I want to manage invoices through the API
  So that I can handle customer billing

  Background:
    Given the API is available
    And I am authenticated as admin

  @smoke
  Scenario: Get all invoices
    When I request all invoices
    Then the response status code should be 200
    And the response should contain a list of invoices

  Scenario: Get invoice by ID
    Given an invoice exists
    When I request the invoice by its ID
    Then the response status code should be 200
    And the response should contain the invoice details
    And the invoice should have line items

  Scenario: Create a new invoice
    Given a customer exists
    And I have invoice details:
      | invoiceDate | dueDate    | notes        |
      | 2024-01-15  | 2024-02-15 | Test invoice |
    And the invoice has item:
      | description   | quantity | unitPrice |
      | Test Service  | 1        | 100.00    |
    When I create the invoice
    Then the response status code should be 201
    And the invoice should be created with status "DRAFT"

  Scenario: Send an invoice
    Given a draft invoice exists
    When I send the invoice
    Then the response status code should be 200
    And the invoice status should be "SENT"
    And a journal entry should be created

  Scenario: Mark invoice as paid
    Given a sent invoice exists
    When I mark the invoice as paid with full amount
    Then the response status code should be 200
    And the invoice status should be "PAID"

  Scenario: Partial payment on invoice
    Given a sent invoice exists with total 1000.00
    When I record a payment of 500.00
    Then the response status code should be 200
    And the paid amount should be 500.00
    And the invoice status should be "SENT"

  Scenario: Cancel an invoice
    Given a draft invoice exists
    When I cancel the invoice
    Then the response status code should be 200
    And the invoice status should be "CANCELLED"

  Scenario: Get invoices by status
    When I request invoices with status "DRAFT"
    Then the response status code should be 200
    And all returned invoices should have status "DRAFT"

  Scenario: Get invoices by customer
    Given a customer exists with invoices
    When I request invoices for the customer
    Then the response status code should be 200
    And all returned invoices should belong to the customer

  @negative
  Scenario: Create invoice without customer
    When I try to create an invoice without a customer
    Then the response status code should be 400
    And the error message should indicate missing customer

  @negative
  Scenario: Send already sent invoice
    Given a sent invoice exists
    When I try to send the invoice again
    Then the response status code should be 400

  @negative
  Scenario: Cancel a paid invoice
    Given a paid invoice exists
    When I try to cancel the invoice
    Then the response status code should be 400

  # Customer management
  @smoke
  Scenario: Get all customers
    When I request all customers
    Then the response status code should be 200
    And the response should contain a list of customers

  Scenario: Create a new customer
    Given I have customer details:
      | name          | email              | phone      |
      | Test Customer | test@customer.com  | 1234567890 |
    When I create the customer
    Then the response status code should be 201
    And the customer should be created successfully

  Scenario: Get customer by ID
    Given a customer exists
    When I request the customer by its ID
    Then the response status code should be 200
    And the response should contain the customer details