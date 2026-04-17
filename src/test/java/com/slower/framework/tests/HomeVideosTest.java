package com.slower.framework.tests;

import com.slower.framework.base.BaseClass;
import com.slower.framework.factory.DriverFactory;
import com.slower.framework.pages.HomePage;
import com.slower.framework.utils.*;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Set;

public class HomeVideosTest extends BaseClass {

    @Test
    public void shouldOpenYoutubeVideoInNewTabTakeScreenshotAndReturnHome() {
        WebDriver driver = DriverFactory.getDriver();
        WaitUtils waits = new WaitUtils(driver);
        ActionUtils actions = new ActionUtils(driver);

        HomePage home = new HomePage(driver);
        Assert.assertTrue(home.getCurrentUrl().contains("beta.solwerindia.com"), "Unexpected URL: " + home.getCurrentUrl());

        String originalHandle = driver.getWindowHandle();
        String homeUrl = driver.getCurrentUrl();
        Duration timeout = Duration.ofSeconds(ConfigReader.getTimeoutSeconds());

        // Try the first visible YouTube link on the home page.
        Set<String> beforeHandles = driver.getWindowHandles();
        actions.click(home.youtubeVideoLinkByIndex(1));

        String outcome;
        try {
            outcome = WindowUtils.waitForNewWindowOrUrlChange(driver, beforeHandles, homeUrl, timeout);
        } catch (TimeoutException e) {
            ScreenshotUtil.captureFullPage(driver, "home_video_no_nav");
            Assert.fail("Video click did not open YouTube window/tab or navigate.");
            return;
        }

        if (outcome != null && outcome.startsWith("WINDOW:")) {
            String newHandle = outcome.substring("WINDOW:".length());
            WindowUtils.switchTo(driver, newHandle);
        }

        waits.waitForPageLoad();
        // Wait for YouTube to load enough for a screenshot (no Thread.sleep).
        ScreenshotUtil.waitUntil(
                driver,
                () -> {
                    String url = driver.getCurrentUrl();
                    return url != null && (url.contains("youtube.com") || url.contains("youtu.be"));
                },
                timeout,
                "Timed out waiting for YouTube URL"
        );
        waits.waitForDuration(Duration.ofSeconds(2));

        ScreenshotUtil.captureFullPage(driver, "youtube_video");

        if (!driver.getWindowHandle().equals(originalHandle)) {
            driver.close();
            WindowUtils.switchTo(driver, originalHandle);
        } else {
            driver.navigate().back();
        }

        waits.waitForPageLoad();
        if (homeUrl != null && !driver.getCurrentUrl().equals(homeUrl)) {
            driver.get(homeUrl);
            waits.waitForPageLoad();
        }
    }
}

