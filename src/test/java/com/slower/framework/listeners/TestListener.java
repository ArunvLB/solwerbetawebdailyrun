package com.slower.framework.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.slower.framework.factory.DriverFactory;
import com.slower.framework.utils.ConfigReader;
import com.slower.framework.utils.ExtentManager;
import com.slower.framework.utils.ReportManager;
import com.slower.framework.utils.ScreenshotUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import com.slower.framework.utils.VideoRecorderUtil;

import java.util.Arrays;

public class TestListener implements ITestListener, ISuiteListener {
    private static final Logger log = LogManager.getLogger(TestListener.class);
    private ExtentReports extent;
    private ThreadLocal<VideoRecorderUtil> recorder = new ThreadLocal<>();

    @Override
    public void onStart(ISuite suite) {
        extent = ExtentManager.getExtent();
        log.info("Suite started: {}", suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("Suite finished: {}", suite.getName());
        ExtentManager.flush();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        ExtentTest test = extent.createTest(testName);
        if (result.getMethod().getGroups() != null && result.getMethod().getGroups().length > 0) {
            test.assignCategory(Arrays.asList(result.getMethod().getGroups()).toArray(new String[0]));
        }
        ReportManager.setTest(test);
        ReportManager.logInfo("Test started");

        if (ConfigReader.isVideoRecordingEnabled()) {
            try {
                VideoRecorderUtil vr = new VideoRecorderUtil();
                vr.startRecord(testName);
                recorder.set(vr);
            } catch (Exception e) {
                log.warn("Failed to start video recording", e);
            }
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ReportManager.logPass("Test passed");
        tryStopRecording();
        ReportManager.unload();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String screenshot = null;
        try {
            if (ConfigReader.isFullPageScreenshotOnFailure()) {
                screenshot = ScreenshotUtil.captureFullPage(DriverFactory.getDriver(), result.getMethod().getMethodName());
            } else {
                screenshot = ScreenshotUtil.capture(DriverFactory.getDriver(), result.getMethod().getMethodName());
            }
        } catch (Exception ignored) {
        }
        ReportManager.logFail("Test failed: " + result.getThrowable(), screenshot);
        tryStopRecording();
        ReportManager.unload();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ReportManager.logInfo("Test skipped: " + result.getThrowable());
        tryStopRecording();
        ReportManager.unload();
    }

    private void tryStopRecording() {
        if (recorder.get() != null) {
            try {
                recorder.get().stopRecord();
            } catch (Exception e) {
                log.warn("Failed to stop video recording", e);
            }
            recorder.remove();
        }
    }

    @Override
    public void onStart(ITestContext context) {
        log.info("Test context started: {}", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("Test context finished: {}", context.getName());
    }
}
