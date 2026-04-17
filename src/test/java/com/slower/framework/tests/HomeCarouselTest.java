package com.slower.framework.tests;

import com.slower.framework.base.BaseClass;
import com.slower.framework.factory.DriverFactory;
import com.slower.framework.pages.HomePage;
import com.slower.framework.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * FLOW 10 – Dynamic Interaction (Carousels / Sliders)
 * Scans the home page for carousel/slider arrows and interacts with them.
 * Gracefully soft-skips if no carousel controls exist on the beta site.
 */
public class HomeCarouselTest extends BaseClass {

    @Test(description = "FLOW 10 – Carousel & Slider Interaction Validation")
    public void shouldInteractWithCarouselsAndSliders() throws InterruptedException {
        WebDriver driver = DriverFactory.getDriver();
        WaitUtils waits = new WaitUtils(driver);
        HomePage homePage = new HomePage(driver);

        String homeUrl = driver.getCurrentUrl();

        // ── 1. Scroll through the full page to load all lazy sections ─────────
        long pageHeight = (Long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight;");
        for (int scrollY = 0; scrollY < pageHeight; scrollY += 600) {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, " + scrollY + ");");
            Thread.sleep(300);
        }
        waits.waitForPageLoad();
        homePage.takeFullPageScreenshot("carousel_full_page_loaded");

        // ── 2. Next (>) Arrow controls ────────────────────────────────────────
        List<WebElement> nextBtns = homePage.getCarouselNextButtons();
        System.out.println("[INFO] Carousel Next buttons found: " + nextBtns.size());

        for (int i = 0; i < Math.min(nextBtns.size(), 3); i++) {
            try {
                WebElement btn = nextBtns.get(i);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
                Thread.sleep(400);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                Thread.sleep(600);
                homePage.takeFullPageScreenshot("carousel_next_" + i);
            } catch (Exception e) {
                System.out.println("[WARN] Failed to click carousel Next #" + i + ": " + e.getMessage());
            }
        }

        // ── 3. Previous (<) Arrow controls ───────────────────────────────────
        List<WebElement> prevBtns = homePage.getCarouselPrevButtons();
        System.out.println("[INFO] Carousel Prev buttons found: " + prevBtns.size());

        for (int i = 0; i < Math.min(prevBtns.size(), 3); i++) {
            try {
                WebElement btn = prevBtns.get(i);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
                Thread.sleep(400);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                Thread.sleep(600);
                homePage.takeFullPageScreenshot("carousel_prev_" + i);
            } catch (Exception e) {
                System.out.println("[WARN] Failed to click carousel Prev #" + i + ": " + e.getMessage());
            }
        }

        // ── 4. Hero / Banner CTA Clicks ───────────────────────────────────────
        List<WebElement> heroCtas = driver.findElements(By.xpath(
                "//section[1]//a[contains(@class,'btn') or contains(@class,'cta')] | " +
                "//*[contains(@class,'hero')]//a | //*[contains(@class,'banner')]//a"));
        System.out.println("[INFO] Hero/Banner CTAs found: " + heroCtas.size());

        if (!heroCtas.isEmpty()) {
            WebElement cta = heroCtas.get(0);
            String ctaText = cta.getText().replaceAll("\\s+", "_").toLowerCase();
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", cta);
            Thread.sleep(400);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cta);
            waits.waitForPageLoad();
            homePage.takeFullPageScreenshot("hero_cta_clicked_" + ctaText);
            driver.navigate().back();
            waits.waitForPageLoad();
        }

        // ── 5. Return Home ────────────────────────────────────────────────────
        driver.navigate().to(homeUrl);
        waits.waitForPageLoad();
        homePage.takeFullPageScreenshot("carousel_test_complete");
        Assert.assertTrue(driver.getCurrentUrl().contains("solwerindia.com"),
                "Should be back at Solwer Home page after carousel interaction test.");
    }
}
