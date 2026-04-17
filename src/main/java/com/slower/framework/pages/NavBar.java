package com.slower.framework.pages;

import com.slower.framework.exceptions.FrameworkException;
import com.slower.framework.utils.ActionUtils;
import com.slower.framework.utils.ConfigReader;
import com.slower.framework.utils.WaitUtils;
import com.slower.framework.utils.XPathUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Locale;

public class NavBar {
    private final WebDriver driver;
    private final ActionUtils actions;
    private final WaitUtils waits;

    public NavBar(WebDriver driver) {
        this.driver = driver;
        this.actions = new ActionUtils(driver);
        this.waits = new WaitUtils(driver);
        PageFactory.initElements(driver, this);
    }

    public SolutionsOverviewPage goToSolutionsOverview() {
        waits.waitForPageLoad();

        By solutionsMenu = By.xpath("(//a[" + XPathUtils.ciContains("normalize-space(.)", "solutions") + "]"
                + " | //button[" + XPathUtils.ciContains("normalize-space(.)", "solutions") + "])[1]");
        actions.click(solutionsMenu);

        // Some sites navigate directly on "Solutions" click; others open a dropdown.
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getTimeoutSeconds()));
        boolean navigated = false;
        try {
            navigated = w.until(d -> d.getCurrentUrl().toLowerCase(Locale.ROOT).contains("solution"));
        } catch (Exception ignored) {
        }

        if (!navigated) {
            By solutionsOverview = By.xpath("(//a[" + XPathUtils.ciContains("normalize-space(.)", "overview") + " and "
                    + XPathUtils.ciContains("normalize-space(.)", "solution") + "]"
                    + " | //a[" + XPathUtils.ciContains("normalize-space(.)", "solutions overview") + "]"
                    + " | //a[" + XPathUtils.ciContains("normalize-space(.)", "overview") + "]"
                    + ")[1]");

            actions.click(solutionsOverview);
            w.until(d -> d.getCurrentUrl().toLowerCase(Locale.ROOT).contains("solution"));
        }

        return new SolutionsOverviewPage(driver).waitUntilLoaded();
    }

    public DigitalToolsPage goToDigitalTools() {
        waits.waitForPageLoad();

        By solutionsMenu = By.xpath("(//a[" + XPathUtils.ciContains("normalize-space(.)", "solutions") + "]"
                + " | //button[" + XPathUtils.ciContains("normalize-space(.)", "solutions") + "])[1]");
        actions.click(solutionsMenu);

        By digitalTools = By.xpath("(//a[" + XPathUtils.ciContains("normalize-space(.)", "digital") + " and "
                + XPathUtils.ciContains("normalize-space(.)", "tools") + "]"
                + " | //button[" + XPathUtils.ciContains("normalize-space(.)", "digital") + " and "
                + XPathUtils.ciContains("normalize-space(.)", "tools") + "]"
                + ")[1]");

        actions.click(digitalTools);
        return new DigitalToolsPage(driver).waitUntilLoaded();
    }
}
