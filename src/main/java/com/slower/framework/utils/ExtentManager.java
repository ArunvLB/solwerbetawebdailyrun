package com.slower.framework.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ExtentManager {
    private static ExtentReports extent;

    private ExtentManager() {
    }

    public static synchronized ExtentReports getExtent() {
        if (extent == null) {
            init();
        }
        return extent;
    }

    public static synchronized void init() {
        if (extent != null) {
            return;
        }

        Path reportsDir = Paths.get(System.getProperty("user.dir"), "reports");
        try {
            Files.createDirectories(reportsDir);
        } catch (Exception ignored) {
        }

        String reportPath = reportsDir.resolve("extent-report.html").toString();
        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setDocumentTitle("Daily Automation Report - 6:00 PM IST");
        spark.config().setReportName("Selenium TestNG Automation - Daily 6:00 PM IST");
        spark.config().setTheme(Theme.STANDARD);

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Environment", ConfigReader.getEnvironment());
        extent.setSystemInfo("Browser", ConfigReader.getBrowser());
        extent.setSystemInfo("Schedule", "Daily at 6:00 PM IST");
        extent.setSystemInfo("Run Source", ConfigReader.isCi() ? "GitHub Actions" : "Local");
    }

    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
        }
    }
}
