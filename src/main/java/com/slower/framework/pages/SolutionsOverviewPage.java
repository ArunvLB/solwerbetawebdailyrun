package com.slower.framework.pages;

import com.slower.framework.exceptions.FrameworkException;
import com.slower.framework.utils.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class SolutionsOverviewPage {
    private final WebDriver driver;
    private final ActionUtils actions;
    private final WaitUtils waits;

    public SolutionsOverviewPage(WebDriver driver) {
        this.driver = driver;
        this.actions = new ActionUtils(driver);
        this.waits = new WaitUtils(driver);
        PageFactory.initElements(driver, this);
    }

    public SolutionsOverviewPage waitUntilLoaded() {
        waits.waitForPageLoad();
        new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getTimeoutSeconds()))
                .until(d -> d.getCurrentUrl().toLowerCase(Locale.ROOT).contains("solwerindia.com"));
        return this;
    }

    public SolutionsOverviewPage openBookDemoFor(String solutionName) {
        String needle = solutionName == null ? "" : solutionName.trim();
        if (needle.isBlank()) {
            throw new FrameworkException("Solution name is blank");
        }

        By bookDemo = By.xpath("("
                + "//h3["
                + XPathUtils.ciContains("normalize-space(.)", needle)
                + "]/following-sibling::button[" + XPathUtils.ciContains("normalize-space(.)", "book demo") + "]"
                + " | "
                + "//*["
                + XPathUtils.ciContains("normalize-space(.)", needle)
                + "]"
                + "/ancestor-or-self::*[self::section or self::div][1]"
                + "//button[" + XPathUtils.ciContains("normalize-space(.)", "book demo") + "]"
                + ")[1]");

        scrollIntoView(bookDemo);
        actions.click(bookDemo);
        return this;
    }

    public SolutionsOverviewPage fillBookDemoFormAndSubmit(FormData data, String screenshotNamePrefix) {
        new FormFiller(driver).fillAndSubmitVisibleForm(data);
        ScreenshotUtil.captureFullPage(driver, screenshotNamePrefix + "_after_submit");
        return this;
    }

    public SolutionsOverviewPage navigateBackToOverview(String expectedUrlFragment) {
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.toLowerCase(Locale.ROOT).contains(expectedUrlFragment.toLowerCase(Locale.ROOT))) {
            try {
                // If we're already on the solutions overview, try to close the modal using Escape or a generic close button if one exists
                new org.openqa.selenium.interactions.Actions(driver).sendKeys(org.openqa.selenium.Keys.ESCAPE).perform();
            } catch (Exception ignored) {}
            return this;
        }
        
        driver.navigate().back();
        waits.waitForPageLoad();
        if (expectedUrlFragment != null && !expectedUrlFragment.isBlank()) {
            new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getTimeoutSeconds()))
                    .until(d -> d.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedUrlFragment.toLowerCase(Locale.ROOT)));
        }
        return this;
    }

    private void scrollIntoView(By locator) {
        try {
            List<WebElement> els = driver.findElements(locator);
            for (WebElement el : els) {
                if (el.isDisplayed()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }
}

