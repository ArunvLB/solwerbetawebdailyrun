package com.slower.framework.pages;

import com.slower.framework.utils.ActionUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPage {
    private final ActionUtils actions;

    @FindBy(id = "username")
    private WebElement usernameInput;

    @FindBy(id = "password")
    private WebElement passwordInput;

    @FindBy(id = "loginBtn")
    private WebElement loginButton;

    @FindBy(id = "message")
    private WebElement messageLabel;

    public LoginPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.actions = new ActionUtils(driver);
    }

    public LoginPage enterUsername(String username) {
        actions.sendKeys(usernameInput, "Username", username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        actions.sendKeys(passwordInput, "Password", password);
        return this;
    }

    public LoginPage clickLogin() {
        actions.click(loginButton, "Login button");
        return this;
    }

    public LoginPage login(String username, String password) {
        return enterUsername(username).enterPassword(password).clickLogin();
    }

    public String getMessage() {
        return actions.getText(messageLabel, "Message");
    }
}

