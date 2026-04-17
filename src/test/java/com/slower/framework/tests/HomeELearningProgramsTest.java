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
import java.util.Set;

public class HomeELearningProgramsTest extends BaseClass {
    @Test
    public void shouldGoToElearningProgramsAndSubmitForm() {
        WebDriver driver = DriverFactory.getDriver();
        ActionUtils actions = new ActionUtils(driver);
        WaitUtils waits = new WaitUtils(driver);
        FormFiller formFiller = new FormFiller(driver);
        HomePage home = new HomePage(driver);
        
        String originalHandle = driver.getWindowHandle();
        String homeUrl = driver.getCurrentUrl();
        
        Set<String> beforeHandles = driver.getWindowHandles();
        actions.click(home.elearningDropdown());
        waits.waitForDuration(java.time.Duration.ofMillis(500));
        actions.click(home.elearningProgramsLink());
        waits.waitForPageLoad();

        Set<String> afterHandles = driver.getWindowHandles();
        if (afterHandles.size() > beforeHandles.size()) {
            for (String h : afterHandles) {
                if (!beforeHandles.contains(h)) {
                    driver.switchTo().window(h);
                    break;
                }
            }
        }

        FormData data = new FormData(
                "Jane Doe", "jane.doe@example.com", "0987654321",
                "Learn Inc", null, "Interested in E-Learning Programs"
        );
        formFiller.fillAndSubmitVisibleForm(data);
        
        home.takeFullPageScreenshot("elearning_programs_submitted");
        
        if (!driver.getWindowHandle().equals(originalHandle)) {
            driver.close();
            driver.switchTo().window(originalHandle);
        } else if (!driver.getCurrentUrl().equals(homeUrl)) {
            driver.navigate().back();
            waits.waitForPageLoad();
        }

        if (!driver.getCurrentUrl().equals(homeUrl)) {
            driver.get(homeUrl);
            waits.waitForPageLoad();
        }
    }
}
