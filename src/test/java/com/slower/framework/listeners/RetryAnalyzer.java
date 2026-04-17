package com.slower.framework.listeners;

import com.slower.framework.utils.ConfigReader;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
    private int currentAttempt = 0;

    @Override
    public boolean retry(ITestResult result) {
        int maxRetries = ConfigReader.getRetryCount();
        if (currentAttempt < maxRetries) {
            currentAttempt++;
            return true;
        }
        return false;
    }
}

