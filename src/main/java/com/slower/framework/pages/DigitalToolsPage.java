package com.slower.framework.pages;

import com.slower.framework.exceptions.FrameworkException;
import com.slower.framework.utils.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Locale;

public class DigitalToolsPage {
    private final WebDriver driver;
    private final ActionUtils actions;
    private final WaitUtils waits;

    public DigitalToolsPage(WebDriver driver) {
        this.driver = driver;
        this.actions = new ActionUtils(driver);
        this.waits = new WaitUtils(driver);
        PageFactory.initElements(driver, this);
    }

    public DigitalToolsPage waitUntilLoaded() {
        waits.waitForPageLoad();
        Duration timeout = Duration.ofSeconds(ConfigReader.getTimeoutSeconds());
        new WebDriverWait(driver, timeout).until(d -> {
            String url = d.getCurrentUrl();
            if (url != null) {
                String u = url.toLowerCase(Locale.ROOT);
                if (u.contains("digital") && u.contains("tool")) {
                    return true;
                }
                if (u.contains("/solutions/") && u.contains("digital")) {
                    return true;
                }
            }
            try {
                String body = d.findElement(By.tagName("body")).getText().toLowerCase(Locale.ROOT);
                return body.contains("digital tools");
            } catch (Exception e) {
                return false;
            }
        });
        return this;
    }

    public String takeFullPageScreenshot(String name) {
        waits.waitForPageLoad();
        return ScreenshotUtil.captureFullPage(driver, name);
    }

    public DigitalToolsPage clickTryMeByIndex(int oneBasedIndex) {
        if (oneBasedIndex <= 0) {
            throw new FrameworkException("Index must be 1+");
        }
        actions.click(tryMeLocator(oneBasedIndex));
        return this;
    }

    public By tryMeLocator(int oneBasedIndex) {
        String expr = "("
                + "//a[" + XPathUtils.ciContains("normalize-space(.)", "try now") + "]"
                + " | "
                + "//button[" + XPathUtils.ciContains("normalize-space(.)", "try now") + "]"
                + ")[" + oneBasedIndex + "]";
        return By.xpath(expr);
    }
}

