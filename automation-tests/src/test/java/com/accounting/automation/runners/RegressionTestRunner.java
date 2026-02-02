package com.accounting.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Regression Test Runner - Runs all regression tests
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.accounting.automation.stepdefinitions",
                "com.accounting.automation.hooks"
        },
        plugin = {
                "pretty",
                "html:target/cucumber-reports/regression-cucumber.html",
                "json:target/cucumber-reports/regression-cucumber.json",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        },
        monochrome = true,
        tags = "@regression"
)
public class RegressionTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}