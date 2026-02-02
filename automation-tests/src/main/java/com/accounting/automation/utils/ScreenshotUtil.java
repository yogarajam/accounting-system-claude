package com.accounting.automation.utils;

import com.accounting.automation.config.ConfigReader;
import com.accounting.automation.config.DriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Screenshot Utility
 */
@Slf4j
public class ScreenshotUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ScreenshotUtil() {
    }

    public static String takeScreenshot(String testName) {
        String screenshotPath = ConfigReader.getScreenshotPath();
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String fileName = String.format("%s/%s_%s.png", screenshotPath, testName, timestamp);

        try {
            File screenshotDir = new File(screenshotPath);
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }

            TakesScreenshot ts = (TakesScreenshot) DriverFactory.getDriver();
            File source = ts.getScreenshotAs(OutputType.FILE);
            File destination = new File(fileName);
            FileUtils.copyFile(source, destination);

            log.info("Screenshot saved: {}", fileName);
            return fileName;
        } catch (IOException e) {
            log.error("Failed to take screenshot", e);
            return null;
        }
    }

    public static byte[] takeScreenshotAsBytes() {
        try {
            TakesScreenshot ts = (TakesScreenshot) DriverFactory.getDriver();
            return ts.getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            log.error("Failed to take screenshot as bytes", e);
            return new byte[0];
        }
    }
}