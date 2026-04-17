package com.slower.framework.utils;

import com.slower.framework.exceptions.FrameworkException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

public final class WindowUtils {
    private WindowUtils() {
    }

    public static String waitForNewWindow(WebDriver driver, Set<String> beforeHandles, Duration timeout) {
        WebDriverWait w = new WebDriverWait(driver, timeout);
        return w.until(d -> {
            Set<String> now = d.getWindowHandles();
            for (String h : now) {
                if (!beforeHandles.contains(h)) {
                    return h;
                }
            }
            return null;
        });
    }

    public static String waitForNewWindowOrUrlChange(WebDriver driver, Set<String> beforeHandles, String beforeUrl, Duration timeout) {
        WebDriverWait w = new WebDriverWait(driver, timeout);
        return w.until(d -> {
            Set<String> now = d.getWindowHandles();
            if (now.size() > beforeHandles.size()) {
                for (String h : now) {
                    if (!beforeHandles.contains(h)) {
                        return "WINDOW:" + h;
                    }
                }
            }
            String currentUrl = d.getCurrentUrl();
            if (currentUrl != null && beforeUrl != null && !currentUrl.equals(beforeUrl)) {
                return "URL";
            }
            return null;
        });
    }

    public static void switchTo(WebDriver driver, String handle) {
        try {
            driver.switchTo().window(handle);
        } catch (Exception e) {
            throw new FrameworkException("Failed to switch to window: " + handle, e);
        }
    }
}

