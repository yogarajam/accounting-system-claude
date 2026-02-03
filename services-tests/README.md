# Accounting System - Services Tests

API and Services Testing Framework for the Accounting System using Serenity BDD, RestAssured, Cucumber, and JUnit 5.

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Accounting System application running on http://localhost:8080

## Project Structure

```
services-tests/
├── pom.xml                             # Maven configuration
├── src/
│   ├── main/java/com/accounting/api/
│   │   ├── client/                     # API clients
│   │   │   ├── BaseApiClient.java      # Base HTTP client
│   │   │   ├── AccountApiClient.java   # Account service client
│   │   │   ├── JournalApiClient.java   # Journal service client
│   │   │   ├── InvoiceApiClient.java   # Invoice service client
│   │   │   ├── ReportApiClient.java    # Report service client
│   │   │   └── AuthApiClient.java      # Authentication client
│   │   ├── config/                     # Configuration
│   │   │   ├── TestConfig.java         # Test configuration
│   │   │   └── RestAssuredConfig.java  # RestAssured setup
│   │   └── model/                      # DTOs
│   │       ├── AccountDTO.java
│   │       ├── JournalEntryDTO.java
│   │       ├── InvoiceDTO.java
│   │       └── ReportDTO.java
│   └── test/
│       ├── java/com/accounting/api/
│       │   ├── runner/                 # Test runners
│       │   │   ├── CucumberTestSuite.java
│       │   │   ├── SmokeTestSuite.java
│       │   │   └── RegressionTestSuite.java
│       │   └── steps/                  # Step definitions
│       │       ├── CommonSteps.java
│       │       ├── AccountSteps.java
│       │       ├── JournalSteps.java
│       │       ├── InvoiceSteps.java
│       │       ├── ReportSteps.java
│       │       └── AuthSteps.java
│       └── resources/
│           ├── features/               # Cucumber feature files
│           │   ├── accounts.feature
│           │   ├── journal.feature
│           │   ├── invoices.feature
│           │   ├── reports.feature
│           │   └── authentication.feature
│           ├── config/
│           │   └── test-config.properties
│           ├── serenity.conf
│           └── logback-test.xml
```

## Running Tests

### Start the Application

Before running tests, ensure the accounting system is running:

```bash
cd ../
mvn spring-boot:run
```

### Run All Tests

```bash
mvn clean verify
```

### Run Smoke Tests Only

```bash
mvn clean verify -Psmoke
```

Or using tag filter:
```bash
mvn clean verify -Dcucumber.filter.tags="@smoke"
```

### Run Regression Tests

```bash
mvn clean verify -Pregression
```

### Run Tests by Service

```bash
# Account service tests
mvn clean verify -Paccounts

# Journal service tests
mvn clean verify -Pjournal

# Report service tests
mvn clean verify -Preports
```

### Run Specific Feature

```bash
mvn clean verify -Dcucumber.features="classpath:features/accounts.feature"
```

### Run With Tags

```bash
# Run all except negative tests
mvn clean verify -Dcucumber.filter.tags="not @negative"

# Run smoke and regression
mvn clean verify -Dcucumber.filter.tags="@smoke or @regression"
```

## Test Reports

After running tests, reports are generated at:

- **Serenity Report**: `target/site/serenity/index.html`
- **Cucumber HTML Report**: `target/cucumber-reports/cucumber.html`
- **Cucumber JSON Report**: `target/cucumber-reports/cucumber.json`

To generate only the Serenity aggregate report:
```bash
mvn serenity:aggregate
```

## Configuration

### Test Configuration (test-config.properties)

Edit `src/test/resources/config/test-config.properties`:

```properties
# Base URL of the application
base.url=http://localhost:8080

# Authentication
test.username=admin
test.password=admin123

# Timeouts
connection.timeout=10000
read.timeout=30000
```

### Serenity Configuration (serenity.conf)

Edit `src/test/resources/serenity.conf`:

```hocon
serenity {
    project.name = "Accounting System API Tests"
    tag.failures = "true"
    linked.tags = "issue"
    take.screenshots = FOR_FAILURES
}
```

## Test Tags

- `@smoke` - Quick validation tests
- `@regression` - Full regression tests
- `@accounts` - Account service tests
- `@journal` - Journal entry tests
- `@invoices` - Invoice service tests
- `@reports` - Report service tests
- `@authentication` - Authentication tests
- `@negative` - Negative/error case tests
- `@wip` - Work in progress (excluded from runs)

## API Clients

The framework provides API clients for each service:

### AccountApiClient
- `getAccountsListPage()` - Get accounts list page
- `viewAccount(Long id)` - View account details
- `saveAccountForm(AccountDTO account)` - Create/update account
- `deactivateAccountForm(Long id)` - Deactivate account
- `activateAccountForm(Long id)` - Activate account

### JournalApiClient
- `getJournalListPage()` - Get journal entries list
- `viewEntry(Long id)` - View journal entry
- `getNewEntryForm()` - Get new entry form
- `postEntryForm(Long id)` - Post journal entry
- `voidEntryForm(Long id)` - Void journal entry

### InvoiceApiClient
- `getInvoicesListPage()` - Get invoices list
- `viewInvoice(Long id)` - View invoice details
- `saveInvoiceForm(InvoiceDTO invoice)` - Create/update invoice
- `sendInvoiceForm(Long id)` - Send invoice
- `cancelInvoiceForm(Long id)` - Cancel invoice

### ReportApiClient
- `getTrialBalancePage()` - Get trial balance report
- `getProfitLossPage()` - Get profit and loss report
- `getBalanceSheetPage()` - Get balance sheet report
- `getGeneralLedgerPage()` - Get general ledger report

### AuthApiClient
- `login(String username, String password)` - Login
- `logout()` - Logout
- `getDashboard()` - Access dashboard
- `isLoggedIn()` - Check login status

## Writing New Tests

### 1. Create a Feature File

```gherkin
@myservice @regression
Feature: My Service API
  As an API consumer
  I want to test my service

  Background:
    Given the API is available
    And I am authenticated as admin

  @smoke
  Scenario: Basic operation
    When I request my resource
    Then the response status code should be 200
```

### 2. Create Step Definitions

```java
@Slf4j
public class MyServiceSteps {

    @When("I request my resource")
    public void iRequestMyResource() {
        Response response = myClient.getResource();
        CommonSteps.setLastResponse(response);
    }
}
```

### 3. Create API Client

```java
@Slf4j
public class MyApiClient extends BaseApiClient {

    public Response getResource() {
        return get("/api/myresource");
    }
}
```

## Troubleshooting

### Tests fail to connect
- Verify the application is running on the configured base URL
- Check network connectivity
- Review test-config.properties settings

### Authentication failures
- Verify credentials in test-config.properties
- Check if the test user exists and has proper permissions
- Review application security configuration

### Missing step definitions
- Check that step definition classes are in the correct package
- Verify the glue path in test runners matches

## Tech Stack

- **Java 17** - Programming language
- **Maven** - Build tool
- **Serenity BDD 4.0.40** - BDD reporting framework
- **RestAssured 5.4.0** - REST API testing
- **Cucumber 7.15.0** - BDD test framework
- **JUnit 5.10.1** - Test framework
- **AssertJ 3.25.1** - Fluent assertions
- **Lombok 1.18.30** - Boilerplate reduction
- **Jackson 2.16.1** - JSON processing