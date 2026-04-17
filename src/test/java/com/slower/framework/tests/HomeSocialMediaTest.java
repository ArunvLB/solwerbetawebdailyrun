package com.slower.framework.tests;

import com.slower.framework.base.BaseClass;
import com.slower.framework.factory.DriverFactory;
import com.slower.framework.pages.HomePage;
import com.slower.framework.utils.WaitUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * FLOW 8 – External Social Media & YouTube Validation
 * Checks for social links in the footer, clicks them, validates domain in new tab.
 * Gracefully soft-skips if social links don't exist on the beta site yet.
 */
public class HomeSocialMediaTest extends BaseClass {

    @Test(description = "FLOW 8 – Social Media Icon Validation with Multi-Tab Context Handling")
    public void shouldValidateSocialMediaLinks() throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WaitUtils waits = new WaitUtils(driver);
        HomePage homePage = new HomePage(driver);

        String homeUrl = driver.getCurrentUrl();
        String parentWindow = driver.getWindowHandle();

        // ── 1. Scroll to footer to trigger lazy-loaded social icons ──────────
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        waits.waitForPageLoad();

        // ── 2. YouTube Icon ───────────────────────────────────────────────────
        List<WebElement> ytLinks = driver.findElements(homePage.youtubeSocialLink());
        if (!ytLinks.isEmpty()) {
            String ytHref = ytLinks.get(0).getAttribute("href");
            System.out.println("[INFO] YouTube link found: " + ytHref);
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", ytHref);
            Thread.sleep(2000);
            List<String> allWindows = new ArrayList<>(driver.getWindowHandles());
            allWindows.removeIf(h -> h.equals(parentWindow));
            if (!allWindows.isEmpty()) {
                driver.switchTo().window(allWindows.get(0));
                waits.waitForPageLoad();
                String ytPageUrl = driver.getCurrentUrl();
                homePage.takeFullPageScreenshot("youtube_social_verified");
                Assert.assertTrue(
                        ytPageUrl.contains("youtube.com") || ytPageUrl.contains("youtu.be"),
                        "YouTube social link should open a YouTube URL. Got: " + ytPageUrl);
                driver.close();
                driver.switchTo().window(parentWindow);
            }
        } else {
            System.out.println("[INFO] No YouTube social link found in footer. Soft-skip.");
        }

        // ── 3. LinkedIn / Twitter ─────────────────────────────────────────────
        List<WebElement> socialLinks = homePage.getSocialLinks();
        System.out.println("[INFO] Found " + socialLinks.size() + " LinkedIn/Twitter social links.");

        for (int i = 0; i < socialLinks.size(); i++) {
            String href = socialLinks.get(i).getAttribute("href");
            if (href == null || href.isBlank()) continue;
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", href);
            Thread.sleep(1500);
            List<String> allWindows = new ArrayList<>(driver.getWindowHandles());
            allWindows.removeIf(h -> h.equals(parentWindow));
            if (!allWindows.isEmpty()) {
                driver.switchTo().window(allWindows.get(0));
                waits.waitForPageLoad();
                String socialUrl = driver.getCurrentUrl();
                homePage.takeFullPageScreenshot("social_link_" + i);
                Assert.assertTrue(
                        socialUrl.contains("linkedin.com") || socialUrl.contains("twitter.com") || socialUrl.contains("x.com"),
                        "Social link should navigate to LinkedIn or Twitter. Got: " + socialUrl);
                driver.close();
                driver.switchTo().window(parentWindow);
            }
        }

        // ── 4. Return Home ────────────────────────────────────────────────────
        driver.navigate().to(homeUrl);
        waits.waitForPageLoad();
        homePage.takeFullPageScreenshot("social_test_complete");
        Assert.assertTrue(driver.getCurrentUrl().contains("solwerindia.com"),
                "Should be back at Solwer Home after social media validation test.");
    }
}
