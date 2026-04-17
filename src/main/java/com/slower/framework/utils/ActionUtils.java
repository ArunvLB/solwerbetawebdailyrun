package com.slower.framework.utils;

import com.slower.framework.exceptions.FrameworkException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ActionUtils {
    private static final Logger log = LogManager.getLogger(ActionUtils.class);

    private final WebDriver driver;
    private final WaitUtils waits;

    public ActionUtils(WebDriver driver) {
        this.driver = driver;
        this.waits = new WaitUtils(driver);
    }

    public void click(By locator) {
        click(locator, locator.toString());
    }

    public void click(By locator, String elementName) {
        String step = "Click: " + elementName;
        try {
            maybeSlowMo();
            log.info(step);
            ReportManager.logInfo(step);
            WebElement el = waits.waitForElementClickable(locator);
            try {
                el.click();
            } catch (ElementClickInterceptedException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
            }
        } catch (RuntimeException e) {
            handleAndThrow(step, e);
        }
    }

    public void sendKeys(By locator, String value) {
        String step = "Send keys to: " + locator + " value=" + mask(value);
        try {
            maybeSlowMo();
            log.info(step);
            ReportManager.logInfo(step);
            WebElement el = waits.waitForElementVisible(locator);
            el.clear();
            el.sendKeys(value);
        } catch (RuntimeException e) {
            handleAndThrow(step, e);
        }
    }

    public String getText(By locator) {
        String step = "Get text from: " + locator;
        try {
            maybeSlowMo();
            log.info(step);
            ReportManager.logInfo(step);
            return waits.waitForElementVisible(locator).getText();
        } catch (RuntimeException e) {
            handleAndThrow(step, e);
            return null;
        }
    }

    public boolean isDisplayed(By locator) {
        String step = "Is displayed: " + locator;
        try {
            maybeSlowMo();
            log.info(step);
            ReportManager.logInfo(step);
            return waits.waitForElementVisible(locator).isDisplayed();
        } catch (RuntimeException e) {
            handleAndThrow(step, e);
            return false;
        }
    }

    public void click(WebElement element, String elementName) {
        String step = "Click: " + elementName;
        try {
            maybeSlowMo();
            log.info(step);
            ReportManager.logInfo(step);
            WebElement el = waits.waitForElementClickable(element);
            try {
                el.click();
            } catch (ElementClickInterceptedException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
            }
        } catch (RuntimeException e) {
            handleAndThrow(step, e);
        }
    }

    public void sendKeys(WebElement element, String elementName, String value) {
        String step = "Send keys to: " + elementName + " value=" + mask(value);
        try {
            maybeSlowMo();
            log.info(step);
            ReportManager.logInfo(step);
            WebElement el = waits.waitForElementVisible(element);
            el.clear();
            el.sendKeys(value);
        } catch (RuntimeException e) {
            handleAndThrow(step, e);
        }
    }

    public String getText(WebElement element, String elementName) {
        String step = "Get text from: " + elementName;
        try {
            maybeSlowMo();
            log.info(step);
            ReportManager.logInfo(step);
            return waits.waitForElementVisible(element).getText();
        } catch (RuntimeException e) {
            handleAndThrow(step, e);
            return null;
        }
    }

    private void handleAndThrow(String step, RuntimeException e) {
        log.error(step + " FAILED", e);
        String screenshotPath = null;
        try {
            screenshotPath = ScreenshotUtil.capture(driver, "failure");
        } catch (Exception ignored) {
        }
        ReportManager.logFail(step + " FAILED: " + e.getMessage(), screenshotPath);
        throw new FrameworkException(step + " failed", e);
    }

    private String mask(String value) {
        if (value == null) {
            return "null";
        }
        if (value.isBlank()) {
            return "\"\"";
        }
        return "***";
    }

    private void maybeSlowMo() {
        long ms = ConfigReader.getSlowMoMs();
        if (ms <= 0) {
            return;
        }
        waits.waitForDuration(java.time.Duration.ofMillis(ms));
    }
}
