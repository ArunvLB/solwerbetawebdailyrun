package com.slower.framework.tests;

import com.slower.framework.base.BaseClass;
import com.slower.framework.factory.DriverFactory;
import com.slower.framework.pages.HomePage;
import com.slower.framework.utils.ActionUtils;
import com.slower.framework.utils.WaitUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;
import java.util.List;

public class HomeNewsTest extends BaseClass {
    @Test
    public void shouldClickNewsLinksAndNavigateBack() {
        WebDriver driver = DriverFactory.getDriver();
        ActionUtils actions = new ActionUtils(driver);
        WaitUtils waits = new WaitUtils(driver);
        HomePage home = new HomePage(driver);
        
        String homeUrl = driver.getCurrentUrl();
        
        actions.click(home.newsPageLink());
        waits.waitForPageLoad();
        
        List<WebElement> newsLinks = home.getNewsLinks();
        int count = Math.min(8, newsLinks.size());
        
        for (int i = 0; i < count; i++) {
            List<WebElement> links = home.getNewsLinks(); // Re-fetch to avoid StaleElement
            if (i < links.size()) {
                actions.click(links.get(i), "News Link " + i);
                waits.waitForPageLoad();
                home.takeFullPageScreenshot("news_article_" + (i + 1));
                driver.navigate().back();
                waits.waitForPageLoad();
            }
        }
        
        if (!driver.getCurrentUrl().equals(homeUrl)) {
            driver.get(homeUrl);
            waits.waitForPageLoad();
        }
    }
}
