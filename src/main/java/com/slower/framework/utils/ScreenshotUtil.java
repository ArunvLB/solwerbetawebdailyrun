package com.slower.framework.utils;

import com.slower.framework.exceptions.FrameworkException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtil {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtil() {
    }

    public static String capture(WebDriver driver, String name) {
        if (driver == null) {
            return null;
        }
        try {
            Path screenshotsDir = Paths.get(System.getProperty("user.dir"), "screenshots");
            Files.createDirectories(screenshotsDir);

            String safeName = (name == null || name.isBlank()) ? "screenshot" : name.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = screenshotsDir.resolve(safeName + "_" + TS.format(LocalDateTime.now()) + ".png");

            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException | RuntimeException e) {
            throw new FrameworkException("Failed to capture screenshot", e);
        }
    }

    /**
     * Full-page screenshot using JavaScript scroll & stitch (no Thread.sleep).
     * Works across browsers in most cases.
     */
    public static String captureFullPage(WebDriver driver, String name) {
        if (driver == null) {
            return null;
        }
        return FullPageScreenshotUtil.capture(driver, buildName(name));
    }

    static String buildName(String name) {
        String safeName = (name == null || name.isBlank()) ? "screenshot" : name.replaceAll("[^a-zA-Z0-9._-]", "_");
        return safeName + "_" + TS.format(LocalDateTime.now());
    }

    static Path screenshotsDir() {
        try {
            Path screenshotsDir = Paths.get(System.getProperty("user.dir"), "screenshots");
            Files.createDirectories(screenshotsDir);
            return screenshotsDir;
        } catch (IOException e) {
            throw new FrameworkException("Failed to create screenshots directory", e);
        }
    }

    public static void waitUntil(WebDriver driver, java.util.function.BooleanSupplier condition, Duration timeout, String errorMessage) {
        try {
            new WebDriverWait(driver, timeout).until(d -> condition.getAsBoolean());
        } catch (RuntimeException e) {
            throw new FrameworkException(errorMessage, e);
        }
    }
}
