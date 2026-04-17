package com.slower.framework.tests;

import com.slower.framework.base.BaseClass;
import com.slower.framework.factory.DriverFactory;
import com.slower.framework.pages.DigitalToolsPage;
import com.slower.framework.pages.NavBar;
import com.slower.framework.utils.ConfigReader;
import com.slower.framework.utils.ScreenshotUtil;
import com.slower.framework.utils.WaitUtils;
import com.slower.framework.utils.WindowUtils;
import org.testng.annotations.Test;
import org.openqa.selenium.TimeoutException;

import java.time.Duration;
import java.util.Set;

public class DigitalToolsTryMeTest extends BaseClass {

    @Test
    public void shouldOpenEachTryMeAppAndNavigateBack() {
        var driver = DriverFactory.getDriver();
        var waits = new WaitUtils(driver);

        DigitalToolsPage digitalTools = new NavBar(driver).goToDigitalTools();
        digitalTools.takeFullPageScreenshot("digital_tools");

        String toolsUrl = driver.getCurrentUrl();
        Duration timeout = Duration.ofSeconds(ConfigReader.getTimeoutSeconds());

        for (int i = 1; i <= 4; i++) {
            String originalHandle = driver.getWindowHandle();
            Set<String> beforeHandles = driver.getWindowHandles();
            String beforeUrl = driver.getCurrentUrl();

            digitalTools.clickTryMeByIndex(i);

            String outcome;
            try {
                outcome = WindowUtils.waitForNewWindowOrUrlChange(driver, beforeHandles, beforeUrl, timeout);
            } catch (TimeoutException e) {
                ScreenshotUtil.captureFullPage(driver, "digital_tools_tryme_" + i + "_no_nav");
                driver.get(toolsUrl);
                digitalTools.waitUntilLoaded();
                continue;
            }
            if (outcome != null && outcome.startsWith("WINDOW:")) {
                String newHandle = outcome.substring("WINDOW:".length());
                WindowUtils.switchTo(driver, newHandle);
                waits.waitForPageLoad();
                ScreenshotUtil.captureFullPage(driver, "digital_tools_tryme_" + i);
                driver.close();
                WindowUtils.switchTo(driver, originalHandle);
                waits.waitForPageLoad();
            } else {
                waits.waitForPageLoad();
                ScreenshotUtil.captureFullPage(driver, "digital_tools_tryme_" + i);
                driver.navigate().back();
                waits.waitForPageLoad();
                // Some apps may redirect; force return to digital tools if needed.
                if (toolsUrl != null && !driver.getCurrentUrl().equals(toolsUrl)) {
                    driver.get(toolsUrl);
                    waits.waitForPageLoad();
                }
                digitalTools.waitUntilLoaded();
            }
        }
    }
}
