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

public class HomeELearningBlogsTest extends BaseClass {
    @Test
    public void shouldClickBlogLinksAndNavigateBack() {
        WebDriver driver = DriverFactory.getDriver();
        ActionUtils actions = new ActionUtils(driver);
        WaitUtils waits = new WaitUtils(driver);
        HomePage home = new HomePage(driver);
        
        String homeUrl = driver.getCurrentUrl();
        
        actions.click(home.elearningDropdown());
        waits.waitForDuration(java.time.Duration.ofMillis(500));
        actions.click(home.elearningBlogsLink());
        waits.waitForPageLoad();
        
        List<WebElement> blogLinks = home.getBlogLinks();
        int count = Math.min(2, blogLinks.size());
        
        for (int i = 0; i < count; i++) {
            List<WebElement> links = home.getBlogLinks(); // Re-fetch
            if (i < links.size()) {
                actions.click(links.get(i), "Blog Link " + i);
                waits.waitForPageLoad();
                home.takeFullPageScreenshot("elearning_blog_" + (i + 1));
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
