package com.accounting.automation.utils;

import com.accounting.automation.config.ConfigReader;
import com.accounting.automation.config.DriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Wait Utility
 */
@Slf4j
public class WaitUtil {

    private WaitUtil() {
    }

    public static WebDriverWait getWait() {
        return new WebDriverWait(DriverFactory.getDriver(),
                Duration.ofSeconds(ConfigReader.getExplicitWait()));
    }

    public static WebDriverWait getWait(int seconds) {
        return new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(seconds));
    }

    public static void waitForPageLoad() {
        WebDriver driver = DriverFactory.getDriver();
        getWait().until(d -> ((JavascriptExecutor) d)
                .executeScript("return document.readyState").equals("complete"));
    }

    public static void waitForAjax() {
        WebDriver driver = DriverFactory.getDriver();
        getWait().until(d -> {
            JavascriptExecutor js = (JavascriptExecutor) d;
            return (Boolean) js.executeScript("return jQuery.active == 0");
        });
    }

    public static WebElement waitForVisible(By locator) {
        return getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickable(By locator) {
        return getWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static void waitForInvisible(By locator) {
        getWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public static void waitForTextPresent(By locator, String text) {
        getWait().until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public static void waitForUrlContains(String urlPart) {
        getWait().until(ExpectedConditions.urlContains(urlPart));
    }

    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sleep interrupted", e);
        }
    }
}