package com.slower.framework.utils;

import com.aventstack.extentreports.ExtentTest;

public final class ReportManager {
    private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();

    private ReportManager() {
    }

    public static void setTest(ExtentTest test) {
        TEST.set(test);
    }

    public static ExtentTest getTest() {
        return TEST.get();
    }

    public static void unload() {
        TEST.remove();
    }

    public static void logInfo(String message) {
        ExtentTest t = getTest();
        if (t != null) {
            t.info(message);
        }
    }

    public static void logPass(String message) {
        ExtentTest t = getTest();
        if (t != null) {
            t.pass(message);
        }
    }

    public static void logFail(String message, String screenshotPath) {
        ExtentTest t = getTest();
        if (t == null) {
            return;
        }
        t.fail(message);
        if (screenshotPath != null && !screenshotPath.isBlank()) {
            try {
                t.addScreenCaptureFromPath(screenshotPath);
            } catch (Exception ignored) {
            }
        }
    }
}

