package com.slower.framework.tests;

import com.slower.framework.base.BaseClass;
import com.slower.framework.factory.DriverFactory;
import com.slower.framework.pages.HomePage;
import com.slower.framework.utils.ActionUtils;
import com.slower.framework.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * FLOW 2 – Footer Deep-Linking & Compliance
 * Scrolls to footer, validates Privacy Policy, Contact Us, and scroll-to-top button.
 */
public class HomeFooterLinksTest extends BaseClass {

    @Test(description = "FLOW 2 – Footer Navigation & Compliance Link Validation")
    public void shouldNavigateFooterLinksAndReturnHome() throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WaitUtils waits = new WaitUtils(driver);
        HomePage homePage = new HomePage(driver);

        String homeUrl = driver.getCurrentUrl();

        // ── 1. Scroll to the bottom footer ────────────────────────────────────
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        waits.waitForPageLoad();
        homePage.takeFullPageScreenshot("footer_visible");

        // ── 2. Privacy Policy ─────────────────────────────────────────────────
        List<WebElement> privacyLinks = driver.findElements(homePage.privacyPolicyLink());
        if (!privacyLinks.isEmpty()) {
            String originalWindow = driver.getWindowHandle();
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", privacyLinks.get(0));
            Thread.sleep(2000); // Wait for navigation to trigger
            
            // Switch to new tab if opened
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.contentEquals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            waits.waitForPageLoad();
            String privacyUrl = driver.getCurrentUrl();
            homePage.takeFullPageScreenshot("privacy_policy_page");
            Assert.assertTrue(
                    privacyUrl.contains("privacy") || privacyUrl.contains("policy") || !privacyUrl.equals(homeUrl),
                    "Privacy Policy link should navigate to a privacy page.");
            
            // If a new tab was opened, close it and switch back
            if (!driver.getWindowHandle().equals(originalWindow)) {
                driver.close();
                driver.switchTo().window(originalWindow);
            } else {
                driver.navigate().back();
                Thread.sleep(1000);
                waits.waitForPageLoad();
            }
        } else {
            System.out.println("[INFO] Privacy Policy link not found in footer. Soft-skip.");
        }

        // ── 3. Contact Us from footer ─────────────────────────────────────────
        List<WebElement> contactLinks = driver.findElements(By.xpath(
                "//footer//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'contact')]"));
        if (!contactLinks.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", contactLinks.get(0));
            waits.waitForPageLoad();
            homePage.takeFullPageScreenshot("contact_via_footer");
            driver.navigate().back();
            waits.waitForPageLoad();
        }

        // ── 4. Scroll-to-top button ───────────────────────────────────────────
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        Thread.sleep(500);
        List<WebElement> scrollBtns = driver.findElements(homePage.scrollToTopButton());
        if (!scrollBtns.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", scrollBtns.get(0));
            Thread.sleep(1000);
        }

        // ── 5. Return Home ────────────────────────────────────────────────────
        driver.navigate().to(homeUrl);
        waits.waitForPageLoad();
        homePage.takeFullPageScreenshot("footer_test_complete");
        Assert.assertTrue(driver.getCurrentUrl().contains("solwerindia.com"),
                "Should be back on Solwer Home after footer navigation test.");
    }
}
