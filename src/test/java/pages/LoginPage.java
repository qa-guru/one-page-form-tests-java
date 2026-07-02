package pages;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

public class LoginPage {

    private final SelenideElement loginInput = $("[data-testid='login-input']");
    private final SelenideElement passwordInput = $("[data-testid='password-input']");
    private final SelenideElement submitButton = $("[data-testid='submit-button']");
    private final SelenideElement formTitle = $("[data-testid='login-form-title']");
    private final SelenideElement errorMessage = $("[data-testid='error-message']");

    @Step("Open login page")
    public LoginPage openPage() {
        open("/login.html");
        return this;
    }

    @Step("Fill and submit form")
    public LogedInPage fillAndSubmitForm(String username, String password) {
        typeUsername(username);
        typePassword(password);
        return submit();
    }
    
    @Step("Type username: {username}")
    public LoginPage typeUsername(String username) {
        loginInput.setValue(username);
        return this;
    }

    @Step("Type password")
    public LoginPage typePassword(String password) {
        passwordInput.setValue(password);
        return this;
    }

    @Step("Submit login form")
    public LogedInPage submit() {
        submitButton.click();
        return new LogedInPage();
    }

    @Step("Verify form title message: {message}")
    public LoginPage shouldHaveFormTitle(String message) {
        formTitle.shouldHave(text(message));
        return this;
    }

    @Step("Verify error message: {message}")
    public LoginPage shouldHaveErrorMessage(String message) {
        errorMessage.shouldHave(text(message));
        return this;
    }
}
