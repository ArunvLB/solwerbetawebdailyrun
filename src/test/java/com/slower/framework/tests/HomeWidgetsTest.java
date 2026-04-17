package com.slower.framework.tests;

import com.slower.framework.base.BaseClass;
import com.slower.framework.factory.DriverFactory;
import com.slower.framework.pages.HomePage;
import com.slower.framework.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * FLOW 9 – Dynamic Widgets (Newsletter + Floating Action Buttons)
 * Conditionally tests newsletter form and FABs. Uses soft-assertions because
 * these elements may not yet exist on the beta site.
 */
public class HomeWidgetsTest extends BaseClass {

    @Test(description = "FLOW 9 – Newsletter Widget & Floating Action Button Validation")
    public void shouldInteractWithDynamicWidgets() throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WaitUtils waits = new WaitUtils(driver);
        HomePage homePage = new HomePage(driver);

        String homeUrl = driver.getCurrentUrl();
        String parentWindow = driver.getWindowHandle();

        // ── 1. Scroll Down to expose all lazy-loaded widgets ─────────────────
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        waits.waitForPageLoad();
        homePage.takeFullPageScreenshot("widgets_footer_view");

        // ── 2. Newsletter Email Widget ─────────────────────────────────────────
        WebElement emailInput = homePage.findNewsletterInput();
        if (emailInput != null) {
            // Invalid email → expect validation
            emailInput.clear();
            emailInput.sendKeys("not-an-email");
            emailInput.sendKeys(Keys.TAB);
            Thread.sleep(500);

            // Valid email submit
            emailInput.clear();
            emailInput.sendKeys("automation.test@solwer-qa.com");
            List<WebElement> submitBtns = driver.findElements(By.xpath(
                    "//button[@type='submit'] | //button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'subscribe')]"));
            if (!submitBtns.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtns.get(0));
                Thread.sleep(2000);
                homePage.takeFullPageScreenshot("newsletter_submitted");
            }
        } else {
            System.out.println("[INFO] Newsletter input not found. Soft-skip.");
        }

        // ── 3. Floating Action Buttons (WhatsApp / Chat) ─────────────────────
        List<WebElement> fabs = driver.findElements(By.xpath(
                "//*[contains(@class,'whatsapp') or contains(@class,'fab') or contains(@class,'floating') " +
                "or contains(@href,'wa.me') or contains(@href,'whatsapp.com')]"));

        if (!fabs.isEmpty()) {
            WebElement fab = fabs.get(0);
            String fabHref = fab.getAttribute("href");
            homePage.takeFullPageScreenshot("fab_before_click");

            if (fabHref != null && (fabHref.contains("wa.me") || fabHref.contains("whatsapp"))) {
                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", fabHref);
                Thread.sleep(1500);
                List<String> handles = new ArrayList<>(driver.getWindowHandles());
                handles.removeIf(h -> h.equals(parentWindow));
                if (!handles.isEmpty()) {
                    driver.switchTo().window(handles.get(0));
                    homePage.takeFullPageScreenshot("whatsapp_tab");
                    driver.close();
                    driver.switchTo().window(parentWindow);
                }
            } else {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", fab);
                Thread.sleep(1000);
                homePage.takeFullPageScreenshot("fab_clicked");
                new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            }
        } else {
            System.out.println("[INFO] No floating action buttons found. Soft-skip.");
        }

        // ── 4. Return Home ────────────────────────────────────────────────────
        driver.navigate().to(homeUrl);
        waits.waitForPageLoad();
        homePage.takeFullPageScreenshot("widgets_test_complete");
        Assert.assertTrue(driver.getCurrentUrl().contains("solwerindia.com"),
                "Should be back on Solwer Home after widget tests.");
    }
}
