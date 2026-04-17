package com.slower.framework.tests;

import com.slower.framework.base.BaseClass;
import com.slower.framework.factory.DriverFactory;
import com.slower.framework.pages.HomePage;
import com.slower.framework.utils.ActionUtils;
import com.slower.framework.utils.FormData;
import com.slower.framework.utils.FormFiller;
import com.slower.framework.utils.WaitUtils;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HomeBrochureTest extends BaseClass {
    @Test
    public void shouldDownloadBrochureAndReturnToHome() {
        WebDriver driver = DriverFactory.getDriver();
        ActionUtils actions = new ActionUtils(driver);
        WaitUtils waits = new WaitUtils(driver);
        FormFiller formFiller = new FormFiller(driver);
        HomePage home = new HomePage(driver);
        
        String homeUrl = driver.getCurrentUrl();
        
        actions.click(home.brouchersLink());
        
        FormData data = new FormData(
                "John Doe", "john.doe@example.com", "1234567890",
                "Test Corp", "2024-12-01T10:00", "Looking for a brochure"
        );
        formFiller.fillAndSubmitVisibleForm(data);
        
        home.takeFullPageScreenshot("brochure_submitted");
        
        if (!driver.getCurrentUrl().equals(homeUrl)) {
            driver.get(homeUrl);
            waits.waitForPageLoad();
        }
    }
}
