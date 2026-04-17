package com.slower.framework.tests;

import com.slower.framework.base.BaseClass;
import com.slower.framework.factory.DriverFactory;
import com.slower.framework.pages.HomePage;
import com.slower.framework.utils.ActionUtils;
import com.slower.framework.utils.WaitUtils;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

public class HomeJoinUsTest extends BaseClass {
    @Test
    public void shouldClickJoinUsAndReturnToHome() {
        WebDriver driver = DriverFactory.getDriver();
        ActionUtils actions = new ActionUtils(driver);
        WaitUtils waits = new WaitUtils(driver);
        HomePage home = new HomePage(driver);
        
        String homeUrl = driver.getCurrentUrl();
        
        actions.click(home.joinUsLink());
        waits.waitForPageLoad();
        home.takeFullPageScreenshot("join_us_page");
        
        if (!driver.getCurrentUrl().equals(homeUrl)) {
            driver.get(homeUrl);
            waits.waitForPageLoad();
        }
    }
}
