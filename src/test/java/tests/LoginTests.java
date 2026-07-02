package tests;

import annotations.Layer;
import static io.qameta.allure.Allure.step;
import io.qameta.allure.Severity;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.selenide.AllureSelenide;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.codeborne.selenide.logevents.SelenideLogger;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;

@Layer("e2e")
@Epic("One Page Form")
@Feature("Login")
@DisplayName("Login")
public class LoginTests extends TestBase {

    // @Test
    // @Tag("positive")
    // @DisplayName("Successful authorization with valid credentials")
    // void successfulAuthorizationTest() {
    //     open("/login.html");

    //     $("[data-testid=login-input]").setValue("user1");
    //     $("[data-testid=password-input]").setValue("password1");
    //     $("[data-testid=submit-button]").click();

    //     $("[data-testid=welcome-message]").shouldHave(text("Welcome, user1!"));
    // }

    @Test
    @Tag("smoke")
    @Tag("positive")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("User is logged in with valid credentials")
    void shouldLoginWithValidCredentials() {
        loginPage.openPage()
                .fillAndSubmitForm("user1", "password1");
        logedInPage.shouldHaveWelcomeMessage("Welcome, user1!");
    }

    @Test
    @Tag("negative")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Authorization fails with wrong password")
    void wrongPasswordAuthorizationTest() {
        open("/login.html");

        $("[data-testid=login-input]").setValue("user1");
        $("[data-testid=password-input]").setValue("WRONG PASSWORD");
        $("[data-testid=submit-button]").click();

        $("[data-testid=error-message]").shouldHave(text("Wrong login or password"));
    }

    @Test
    @Tag("negative")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Authorization fails with empty password")
    void emptyPasswordAuthorizationTest() {
        step("Open login page", () -> 
            open("/login.html"));

        step("Fill and submit form", () -> {
            step("Type username", () -> 
                $("[data-testid=login-input]").setValue("user1"));
            step("Submit login form", () -> 
                $("[data-testid=submit-button]").click());
        });

        step("Verify error message", () -> $("[data-testid=error-message]").shouldHave(
            text("Password is required (minimum 6 characters") ));
    }
    
    @Test
    @Tag("negative")
    @DisplayName("Authorization fails with empty login")
    void emptyLoginAuthorizationTest() {
        if (!config.enableAllureSelenideStepsListener()) {
            SelenideLogger.removeListener("AllureSelenide");
        }
        
        step("Open login page", () -> 
            open("/login.html"));

        step("Type username", () -> 
            $("[data-testid=login-input]").setValue("user1"));
        step("Submit login form", () -> 
            $("[data-testid=submit-button]").click());

        step("Verify error message", () -> $("[data-testid=error-message]").shouldHave(
            text("Login is required (minimum 3 characters")));

        if (config.enableAllureSelenideStepsListener()) {
            SelenideLogger.addListener("AllureSelenide",
                new AllureSelenide()
                        .screenshots(false)
                        .savePageSource(false));
        }
        
    }

}
