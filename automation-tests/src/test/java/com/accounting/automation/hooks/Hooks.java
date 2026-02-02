package com.accounting.automation.hooks;

import com.accounting.automation.config.ConfigReader;
import com.accounting.automation.config.DriverFactory;
import com.accounting.automation.utils.ScreenshotUtil;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;

/**
 * Cucumber Hooks - Before and After each scenario
 */
@Slf4j
public class Hooks {

    @Before
    public void setUp(Scenario scenario) {
        log.info("========================================");
        log.info("Starting Scenario: {}", scenario.getName());
        log.info("Tags: {}", scenario.getSourceTagNames());
        log.info("========================================");

        DriverFactory.initDriver();
    }

    @After
    public void tearDown(Scenario scenario) {
        log.info("========================================");
        log.info("Finished Scenario: {}", scenario.getName());
        log.info("Status: {}", scenario.getStatus());
        log.info("========================================");

        if (scenario.isFailed()) {
            log.error("Scenario FAILED: {}", scenario.getName());

            if (ConfigReader.takeScreenshotOnFailure()) {
                try {
                    byte[] screenshot = ScreenshotUtil.takeScreenshotAsBytes();
                    scenario.attach(screenshot, "image/png", "Failure Screenshot");
                    log.info("Screenshot attached to report");
                } catch (Exception e) {
                    log.error("Failed to attach screenshot", e);
                }
            }
        }

        DriverFactory.quitDriver();
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        if (scenario.isFailed()) {
            log.debug("Step failed in scenario: {}", scenario.getName());
        }
    }
}