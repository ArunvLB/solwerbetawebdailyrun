package com.slower.framework.factory;

import com.slower.framework.exceptions.FrameworkException;
import com.slower.framework.utils.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.time.Duration;
import java.util.Locale;

public final class DriverFactory {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() {
    }

    public static void initDriver(String browserOverride) {
        if (DRIVER.get() != null) {
            return;
        }

        String browser = (browserOverride == null || browserOverride.isBlank())
                ? ConfigReader.getBrowser()
                : browserOverride;
        browser = browser.trim().toLowerCase(Locale.ROOT);

        WebDriver driver;
        switch (browser) {
            case "chrome" -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                applyCommonChromiumOptions(options);
                driver = new ChromeDriver(options);
            }
            case "edge" -> {
                WebDriverManager.edgedriver().setup();
                EdgeOptions options = new EdgeOptions();
                applyCommonChromiumOptions(options);
                driver = new EdgeDriver(options);
            }
            default -> throw new FrameworkException("Unsupported browser: " + browser + " (supported: chrome, edge)");
        }

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getPageLoadTimeoutSeconds()));
        DRIVER.set(driver);
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new FrameworkException("WebDriver not initialized. Ensure DriverFactory.initDriver() is called.");
        }
        return driver;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        try {
            if (driver != null) {
                driver.quit();
            }
        } finally {
            DRIVER.remove();
        }
    }

    private static void applyCommonChromiumOptions(org.openqa.selenium.chromium.ChromiumOptions<?> options) {
        String browserBinaryPath = ConfigReader.getBrowserBinaryPath();
        if (browserBinaryPath != null && !browserBinaryPath.isBlank()) {
            options.setBinary(browserBinaryPath);
        }

        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        if (ConfigReader.isCi()) {
            options.addArguments("--no-sandbox");
        }

        if (ConfigReader.isHeadless()) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        } else {
            options.addArguments("--start-maximized");
        }
    }
}
