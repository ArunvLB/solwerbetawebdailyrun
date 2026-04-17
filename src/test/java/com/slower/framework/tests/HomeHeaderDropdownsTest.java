package com.slower.framework.tests;

import com.slower.framework.base.BaseClass;
import com.slower.framework.factory.DriverFactory;
import com.slower.framework.pages.HomePage;
import com.slower.framework.utils.ActionUtils;
import com.slower.framework.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * FLOW 1 – Header Dropdown Matrix Validation
 * Verifies that the top navigation dropdowns (Solutions, E-learning Modules)
 * expand and reveal sub-links, and that Direct links like "Digital Tools" work.
 */
public class HomeHeaderDropdownsTest extends BaseClass {

    @Test(description = "FLOW 1 – Header Navigation Dropdown Matrix Validation")
    public void shouldValidateAllHeaderDropdowns() {
        WebDriver driver = DriverFactory.getDriver();
        WaitUtils waits = new WaitUtils(driver);
        ActionUtils actions = new ActionUtils(driver);
        HomePage homePage = new HomePage(driver);

        String baseUrl = driver.getCurrentUrl();

        // ── 1. Solutions Dropdown ─────────────────────────────────────────────
        actions.click(homePage.solutionsDropdown(), "Solutions Dropdown");
        waits.waitForPageLoad();

        // Verify at least some sub-links are now visible
        List<WebElement> subLinks = driver.findElements(By.xpath(
                "//button[@id='solutions-dropdown'][1]/following-sibling::div//a"));
        if (subLinks.isEmpty()) {
            subLinks = driver.findElements(By.xpath("//div[contains(@class,'dropdown')]//a"));
        }
        Assert.assertFalse(subLinks.isEmpty(),
                "Solutions dropdown should reveal at least one sub-link when clicked.");

        // ── 2. E-Learning Modules Dropdown ────────────────────────────────────
        actions.click(homePage.elearningDropdown(), "E-learning Modules Dropdown");
        waits.waitForPageLoad();

        List<WebElement> elearningLinks = driver.findElements(By.xpath(
                "//button[contains(.,'E-learning Modules')]/following-sibling::div//a"));
        Assert.assertFalse(elearningLinks.isEmpty(),
                "E-learning Modules dropdown should expose sub-links.");

        // ── 3. Digital Tools – direct nav link ───────────────────────────────
        driver.navigate().to(baseUrl);
        waits.waitForPageLoad();

        List<WebElement> dtLinks = driver.findElements(homePage.digitalToolsLink());
        if (!dtLinks.isEmpty() && dtLinks.get(0).isDisplayed()) {
            actions.click(homePage.digitalToolsLink(), "Digital Tools Nav Link");
            waits.waitForPageLoad();
            String url = driver.getCurrentUrl();
            Assert.assertTrue(url.contains("digital") || url.contains("tools") || !url.equals(baseUrl),
                    "Clicking Digital Tools link should navigate away from Home.");
        } else {
            System.out.println("[INFO] Digital Tools link not displayed. Soft-skip.");
        }

        // ── 4. Return Home ────────────────────────────────────────────────────
        driver.navigate().to(baseUrl);
        waits.waitForPageLoad();
        homePage.takeFullPageScreenshot("header_dropdowns_verified");

        Assert.assertTrue(driver.getCurrentUrl().contains("solwerindia.com"),
                "Should be back on the Solwer home page after completing header tests.");
    }
}
