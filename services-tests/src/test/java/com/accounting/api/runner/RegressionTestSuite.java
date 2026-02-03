package com.accounting.api.runner;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Regression Test Suite - Full regression tests
 *
 * Run with: mvn clean verify -Dtest=RegressionTestSuite
 * Or: mvn clean verify -Dcucumber.filter.tags="@regression"
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
    value = "pretty,html:target/cucumber-reports/regression-cucumber.html,json:target/cucumber-reports/regression-cucumber.json")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME,
    value = "com.accounting.api.steps")
@ConfigurationParameter(key = Constants.FEATURES_PROPERTY_NAME,
    value = "classpath:features")
@ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME,
    value = "@regression and not @wip")
public class RegressionTestSuite {
    // This class runs full regression tests
}