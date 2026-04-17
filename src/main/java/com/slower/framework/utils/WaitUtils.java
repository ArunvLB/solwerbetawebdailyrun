package com.slower.framework.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitUtils {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public WaitUtils(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getTimeoutSeconds()));
    }

    public WebElement waitForElementVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForElementClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public WebElement waitForElementVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    public WebElement waitForElementClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public void waitForPageLoad() {
        wait.until(d -> {
            try {
                Object state = ((JavascriptExecutor) driver).executeScript("return document.readyState");
                return "complete".equals(state);
            } catch (RuntimeException e) {
                return false;
            }
        });
    }

    public void waitForDuration(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return;
        }
        long end = System.nanoTime() + duration.toNanos();
        new FluentWait<>(driver)
                .withTimeout(duration)
                .pollingEvery(Duration.ofMillis(100))
                .ignoring(RuntimeException.class)
                .until(d -> System.nanoTime() >= end);
    }
}
