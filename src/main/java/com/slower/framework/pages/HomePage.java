package com.slower.framework.pages;

import com.slower.framework.utils.ScreenshotUtil;
import com.slower.framework.utils.WaitUtils;
import com.slower.framework.utils.XPathUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import java.util.List;

public class HomePage {
    private final WebDriver driver;
    private final WaitUtils waits;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.waits = new WaitUtils(driver);
        PageFactory.initElements(driver, this);
    }

    public String getTitle() {
        waits.waitForPageLoad();
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        waits.waitForPageLoad();
        return driver.getCurrentUrl();
    }

    public String takeFullPageScreenshot(String name) {
        waits.waitForPageLoad();
        return ScreenshotUtil.captureFullPage(driver, name);
    }

    public By youtubeVideoLinkByIndex(int oneBasedIndex) {
        String xpath = "("
                + "//a[contains(@href,'youtube.com') or contains(@href,'youtu.be')]"
                + " | "
                + "//a[" + XPathUtils.ciContains("normalize-space(.)", "youtube") + "]"
                + ")[" + oneBasedIndex + "]";
        return By.xpath(xpath);
    }

    public By brouchersLink() {
        return By.xpath("(//a[" + XPathUtils.ciContains("normalize-space(.)", "broucher") + " or " + XPathUtils.ciContains("normalize-space(.)", "brochure") + "] | //button[" + XPathUtils.ciContains("normalize-space(.)", "broucher") + " or " + XPathUtils.ciContains("normalize-space(.)", "brochure") + "])[1]");
    }

    public By newsPageLink() {
        return By.xpath("(//a[" + XPathUtils.ciContains("normalize-space(.)", "news") + "])[1]");
    }

    public By elearningDropdown() {
        return By.xpath("//button[contains(., 'E-learning Modules')]");
    }

    public By elearningProgramsLink() {
        return By.xpath("//button[contains(., 'E-learning Modules')]/following-sibling::div//a[contains(@href, 'contact-building')] | //a[contains(@href, 'programs')]");
    }

    public By elearningBlogsLink() {
        return By.xpath("//button[contains(., 'E-learning Modules')]/following-sibling::div//a[contains(@href, 'blogs')]");
    }

    public By joinUsLink() {
        return By.xpath("(//a[" + XPathUtils.ciContains("normalize-space(.)", "join us") + "])[1]");
    }

    public By contactUsLink() {
        return By.xpath("(//a[" + XPathUtils.ciContains("normalize-space(.)", "contact us") + "])[1]");
    }

    public List<org.openqa.selenium.WebElement> getNewsLinks() {
        String xpath = "//a[contains(@href, 'news') or " + XPathUtils.ciContains("normalize-space(.)", "read more") + "]";
        return driver.findElements(By.xpath(xpath));
    }

    public List<org.openqa.selenium.WebElement> getBlogLinks() {
        String xpath = "//a[contains(@class, 'blog-link')]";
        return driver.findElements(By.xpath(xpath));
    }

    // ── Header Dropdown locators ──────────────────────────────────────────────
    public By solutionsDropdown() {
        return By.xpath("(//button[@id='solutions-dropdown'])[1]");
    }

    public By digitalToolsLink() {
        return By.xpath("//a[" + XPathUtils.ciContains("normalize-space(.)", "digital tools") + "] | //button[" + XPathUtils.ciContains("normalize-space(.)", "digital tools") + "]");
    }

    // ── Footer locators ───────────────────────────────────────────────────────
    public By privacyPolicyLink() {
        return By.xpath("//footer//a[" + XPathUtils.ciContains("normalize-space(.)", "privacy policy") + "]"
                + " | //a[" + XPathUtils.ciContains("normalize-space(.)", "privacy policy") + "]");
    }

    public By youtubeSocialLink() {
        return By.xpath("//a[@title='YouTube'] | //a[contains(@href,'youtube.com') and not(contains(@href,'watch'))]");
    }

    public By homeLogoLink() {
        return By.xpath("//a[contains(@href,'/') and (./img[contains(@alt,'logo')] or ./img[contains(@alt,'Solwer')] or " +
                XPathUtils.ciContains("normalize-space(.)", "solwer") + ")]" +
                " | //header//a[@href='/'] | //nav//a[@href='/']");
    }

    // ── Scroll-to-top ─────────────────────────────────────────────────────────
    public By scrollToTopButton() {
        return By.xpath("//button[@id='app-scroll-to-top'] | //button[contains(@class,'scroll-top')]");
    }

    // ── Social icons (conditional – may not exist yet on beta) ───────────────
    public List<org.openqa.selenium.WebElement> getSocialLinks() {
        return driver.findElements(By.xpath(
                "//footer//a[contains(@href,'linkedin.com') or contains(@href,'twitter.com') or contains(@href,'x.com')]"));
    }

    // ── Newsletter widget (conditional) ───────────────────────────────────────
    public org.openqa.selenium.WebElement findNewsletterInput() {
        java.util.List<org.openqa.selenium.WebElement> els = driver.findElements(
                By.xpath("//input[@type='email'] | //input[contains(@placeholder,'email') or contains(@placeholder,'Email')]"));
        return els.isEmpty() ? null : els.get(0);
    }

    // ── Carousel arrows (conditional) ─────────────────────────────────────────
    public List<org.openqa.selenium.WebElement> getCarouselNextButtons() {
        return driver.findElements(By.xpath(
                "//*[contains(@class,'slick-next') or contains(@class,'carousel-next') or contains(@aria-label,'Next')]"));
    }

    public List<org.openqa.selenium.WebElement> getCarouselPrevButtons() {
        return driver.findElements(By.xpath(
                "//*[contains(@class,'slick-prev') or contains(@class,'carousel-prev') or contains(@aria-label,'Previous') or contains(@aria-label,'Prev')]"));
    }
}
