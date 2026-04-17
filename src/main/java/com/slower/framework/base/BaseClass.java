package com.slower.framework.base;

import com.slower.framework.factory.DriverFactory;
import com.slower.framework.utils.ConfigReader;
import com.slower.framework.utils.ExtentManager;
import com.slower.framework.utils.WaitUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

public abstract class BaseClass {
    private static final String CHROME = "chrome";
    private static final String EDGE = "edge";

    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser", "env"})
    public void setUp(@Optional String browser, @Optional String env) {
        String envValue = normalizeTestNgParam(env);
        String browserValue = normalizeBrowserParam(browser);

        if (envValue != null) {
            System.setProperty("env", envValue);
        }
        if (browserValue != null) {
            System.setProperty("browser", browserValue);
        }

        ExtentManager.getExtent();
        DriverFactory.initDriver(null);
        DriverFactory.getDriver().get(ConfigReader.getBaseUrl());
        new WaitUtils(DriverFactory.getDriver()).waitForPageLoad();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (!ConfigReader.keepBrowserOpen()) {
            DriverFactory.quitDriver();
        }
    }

    private String normalizeTestNgParam(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        if (trimmed.startsWith("$")) {
            // Common CI placeholder values like "$Browser" or "${BROWSER}"
            return null;
        }
        return trimmed;
    }

    private String normalizeBrowserParam(String value) {
        String normalized = normalizeTestNgParam(value);
        if (normalized == null) {
            return null;
        }

        String lower = normalized.toLowerCase();
        if (CHROME.equals(lower) || EDGE.equals(lower)) {
            return lower;
        }

        return null;
    }
}
