package pages;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.open;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

public class LogedInPage {

    private final SelenideElement welcomeMessage = $("[data-testid='welcome-message']");
    private final SelenideElement logoutButton = $("[data-testid='logout-button']");
    private final SelenideElement formTitle = $("[data-testid='logged-in-form-title']");

    @Step("Open logged in page")
    public LogedInPage openPage() { 
        // TODO: Implement
        return this;
    }

    @Step("Open logged in page with local storage authentication")
    public LogedInPage openPageWithLocalStorageAuthentication(String username, String password) { 
        open("/login.html"); // TODO: open .svg
        executeJavaScript("localStorage.setItem('username', '" + username + "');");
        executeJavaScript("localStorage.setItem('password', '" + password + "');");
        open("/logged-in.html");
        return this;
    }

    @Step("Verify form title message: {message}")
    public LogedInPage shouldHaveFormTitle(String message) {
        formTitle.shouldHave(text(message));
        return this;
    }

    @Step("Verify welcome message: {message}")
    public LogedInPage shouldHaveWelcomeMessage(String message) {
        welcomeMessage.shouldHave(text(message));
        return this;
    }

    @Step("Click logout button")
    public LoginPage clickLogoutButton() {
        logoutButton.click();
        return new LoginPage();
    }

}
