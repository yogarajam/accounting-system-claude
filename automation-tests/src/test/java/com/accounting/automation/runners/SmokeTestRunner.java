package com.accounting.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Smoke Test Runner - Runs only smoke tests
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.accounting.automation.stepdefinitions",
                "com.accounting.automation.hooks"
        },
        plugin = {
                "pretty",
                "html:target/cucumber-reports/smoke-cucumber.html",
                "json:target/cucumber-reports/smoke-cucumber.json",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        },
        monochrome = true,
        tags = "@smoke"
)
public class SmokeTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}