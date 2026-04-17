package com.slower.framework.tests;

import com.slower.framework.base.BaseClass;
import com.slower.framework.factory.DriverFactory;
import com.slower.framework.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseClass {

    @Test
    public void loginShouldSucceedWithValidCredentials() {
        LoginPage loginPage = new LoginPage(DriverFactory.getDriver());
        loginPage.login("admin", "admin123");
        Assert.assertEquals(loginPage.getMessage(), "Login successful");
    }
}

