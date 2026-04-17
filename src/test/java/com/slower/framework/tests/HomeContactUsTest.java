package com.slower.framework.tests;

import com.slower.framework.base.BaseClass;
import com.slower.framework.factory.DriverFactory;
import com.slower.framework.pages.HomePage;
import com.slower.framework.utils.ActionUtils;
import com.slower.framework.utils.FormData;
import com.slower.framework.utils.FormFiller;
import com.slower.framework.utils.WaitUtils;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

public class HomeContactUsTest extends BaseClass {
    @Test
    public void shouldFillContactUsAndReturnToHome() {
        WebDriver driver = DriverFactory.getDriver();
        ActionUtils actions = new ActionUtils(driver);
        WaitUtils waits = new WaitUtils(driver);
        FormFiller formFiller = new FormFiller(driver);
        HomePage home = new HomePage(driver);
        
        String homeUrl = driver.getCurrentUrl();
        
        actions.click(home.contactUsLink());
        waits.waitForPageLoad();
        
        FormData data = new FormData(
                "James Bond", "bond@mi6.gov.uk", "0070070070",
                "MI6", null, "I have a top secret message."
        );
        formFiller.fillAndSubmitVisibleForm(data);
        
        home.takeFullPageScreenshot("contact_us_submitted");
        
        if (!driver.getCurrentUrl().equals(homeUrl)) {
            driver.get(homeUrl);
            waits.waitForPageLoad();
        }
    }
}
