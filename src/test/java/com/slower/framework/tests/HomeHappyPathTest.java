package com.slower.framework.tests;

import com.slower.framework.base.BaseClass;
import com.slower.framework.factory.DriverFactory;
import com.slower.framework.pages.HomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class HomeHappyPathTest extends BaseClass {

    @Test
    public void homePageShouldLoadAndCaptureFullPageScreenshot() {
        HomePage home = new HomePage(DriverFactory.getDriver());
        Assert.assertTrue(home.getCurrentUrl().contains("beta.solwerindia.com"), "Unexpected URL: " + home.getCurrentUrl());
        Assert.assertFalse(home.getTitle() == null || home.getTitle().isBlank(), "Page title should not be blank");
        home.takeFullPageScreenshot("home_happy");
    }
}

