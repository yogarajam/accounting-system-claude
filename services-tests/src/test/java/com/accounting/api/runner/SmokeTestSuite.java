package com.accounting.api.runner;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Smoke Test Suite - Quick validation tests
 *
 * Run with: mvn clean verify -Dtest=SmokeTestSuite
 * Or: mvn clean verify -Dcucumber.filter.tags="@smoke"
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
    value = "pretty,html:target/cucumber-reports/smoke-cucumber.html,json:target/cucumber-reports/smoke-cucumber.json")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME,
    value = "com.accounting.api.steps")
@ConfigurationParameter(key = Constants.FEATURES_PROPERTY_NAME,
    value = "classpath:features")
@ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME,
    value = "@smoke")
public class SmokeTestSuite {
    // This class runs only smoke tests
}