# Accounting System - Automation Test Framework

A comprehensive Selenium WebDriver automation framework with Cucumber BDD, TestNG, and Maven for testing the Accounting System application.

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Programming Language |
| Selenium WebDriver | 4.16.1 | Browser Automation |
| Cucumber | 7.15.0 | BDD Framework |
| TestNG | 7.9.0 | Test Framework |
| Maven | 3.x | Build Tool |
| WebDriverManager | 5.6.2 | Automatic Driver Management |
| ExtentReports | 5.1.1 | Test Reporting |
| Log4j2 | 2.22.0 | Logging |
| Lombok | 1.18.30 | Reduce Boilerplate |
| JavaFaker | 1.0.2 | Test Data Generation |

## Project Structure

```
automation-tests/
├── pom.xml                              # Maven configuration
├── testng.xml                           # TestNG suite (all tests)
├── testng-smoke.xml                     # Smoke test suite
├── testng-regression.xml                # Regression test suite
├── README.md                            # This file
│
├── src/main/java/com/accounting/automation/
│   ├── config/
│   │   ├── ConfigReader.java           # Configuration properties reader
│   │   └── DriverFactory.java          # WebDriver factory (ThreadLocal)
│   ├── pages/                           # Page Object Model
│   │   ├── BasePage.java               # Base page with common methods
│   │   ├── LoginPage.java
│   │   ├── DashboardPage.java
│   │   ├── AccountsPage.java
│   │   ├── JournalPage.java
│   │   ├── LedgerPage.java
│   │   ├── InvoicesPage.java
│   │   ├── ReportsPage.java
│   │   └── BankPage.java
│   └── utils/
│       ├── ScreenshotUtil.java         # Screenshot utility
│       ├── WaitUtil.java               # Wait utilities
│       └── TestDataGenerator.java      # Faker-based data generator
│
└── src/test/
    ├── java/com/accounting/automation/
    │   ├── hooks/
    │   │   └── Hooks.java              # Cucumber hooks
    │   ├── stepdefinitions/
    │   │   ├── LoginSteps.java
    │   │   ├── AccountSteps.java
    │   │   ├── JournalSteps.java
    │   │   └── ReportSteps.java
    │   ├── runners/
    │   │   ├── TestRunner.java         # Main test runner
    │   │   ├── SmokeTestRunner.java    # Smoke tests
    │   │   └── RegressionTestRunner.java
    │   └── listeners/
    │       └── TestListener.java       # TestNG listener
    │
    └── resources/
        ├── features/                    # Cucumber feature files
        │   ├── login.feature
        │   ├── accounts.feature
        │   ├── journal.feature
        │   ├── reports.feature
        │   └── dashboard.feature
        ├── config.properties           # Test configuration
        ├── log4j2.xml                  # Logging configuration
        ├── extent.properties           # ExtentReports config
        └── extent-config.xml           # ExtentReports theme
```

## Prerequisites

1. **Java 17** or higher
2. **Maven 3.x**
3. **Chrome Browser** (or Firefox/Edge)
4. **Accounting System** running on localhost:8080

## Quick Start

### 1. Start the Accounting System
```bash
cd ../
mvn spring-boot:run
```

### 2. Run All Tests
```bash
cd automation-tests
mvn clean test
```

### 3. Run Smoke Tests
```bash
mvn clean test -P smoke
```

### 4. Run Regression Tests
```bash
mvn clean test -P regression
```

## Running Tests with Different Browsers

```bash
# Chrome (default)
mvn clean test -Dbrowser=chrome

# Firefox
mvn clean test -Dbrowser=firefox

# Edge
mvn clean test -Dbrowser=edge

# Headless Chrome
mvn clean test -Dbrowser=chrome -Dheadless=true
```

## Running Tests with Tags

```bash
# Run only login tests
mvn clean test -Dcucumber.filter.tags="@login"

# Run smoke tests
mvn clean test -Dcucumber.filter.tags="@smoke"

# Run regression tests
mvn clean test -Dcucumber.filter.tags="@regression"

# Exclude WIP tests
mvn clean test -Dcucumber.filter.tags="not @wip"

# Run positive tests only
mvn clean test -Dcucumber.filter.tags="@positive"
```

## Test Reports

After test execution, reports are available at:

| Report | Location |
|--------|----------|
| Cucumber HTML | `target/cucumber-reports/cucumber.html` |
| ExtentReports | `target/extent-reports/SparkReport.html` |
| Screenshots | `target/screenshots/` |
| Logs | `target/logs/automation.log` |

## Configuration

Edit `src/test/resources/config.properties`:

```properties
# Application URL
base.url=http://localhost:8080

# Credentials
username=admin
password=admin

# Browser settings
browser=chrome
headless=false

# Timeouts (seconds)
implicit.wait=10
explicit.wait=15
page.load.timeout=30
```

## Writing New Tests

### 1. Create a Feature File
```gherkin
# src/test/resources/features/my_feature.feature
@myfeature
Feature: My New Feature
  As a user
  I want to do something
  So that I achieve a goal

  Scenario: My test scenario
    Given I am logged in as admin
    When I perform an action
    Then I should see the expected result
```

### 2. Create Step Definitions
```java
// src/test/java/com/accounting/automation/stepdefinitions/MySteps.java
public class MySteps {
    @When("I perform an action")
    public void iPerformAnAction() {
        // Implementation
    }
}
```

### 3. Create Page Object (if needed)
```java
// src/main/java/com/accounting/automation/pages/MyPage.java
public class MyPage extends BasePage {
    @FindBy(id = "myElement")
    private WebElement myElement;

    public void doSomething() {
        click(myElement);
    }
}
```

## Best Practices

1. **Page Object Model**: All page interactions should go through page objects
2. **BDD**: Write feature files in business language
3. **Data-Driven**: Use Scenario Outlines for multiple test data
4. **Tags**: Use tags for organizing and filtering tests
5. **Waits**: Use explicit waits, avoid Thread.sleep()
6. **Logging**: Log important actions for debugging
7. **Screenshots**: Automatically captured on failure

## Troubleshooting

### Driver Issues
WebDriverManager handles driver downloads automatically. If issues occur:
```bash
# Clear WebDriverManager cache
rm -rf ~/.cache/selenium
```

### Port Conflicts
Ensure the application is running on port 8080 before running tests.

### Element Not Found
- Check if element locators are correct
- Increase explicit wait timeout in config.properties
- Ensure the application is fully loaded

## CI/CD Integration

### Jenkins Pipeline Example
```groovy
pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh 'mvn clean test -P smoke -Dheadless=true'
            }
            post {
                always {
                    publishHTML(target: [
                        reportDir: 'target/extent-reports',
                        reportFiles: 'SparkReport.html',
                        reportName: 'Extent Report'
                    ])
                }
            }
        }
    }
}
```

## Contact

For issues and questions, please create an issue in the repository.