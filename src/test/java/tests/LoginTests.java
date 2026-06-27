package tests;

import annotations.Layer;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;

@Layer("e2e")
@Epic("One Page Form")
@Feature("Login")
@DisplayName("Login")
public class LoginTests extends TestBase {

    @Test
    @Tag("positive")
    @DisplayName("Successful authorization with valid credentials")
    void successfulAuthorizationTest() {
        open("/login.html");

        $("[data-testid=login-input]").setValue("user1");
        $("[data-testid=password-input]").setValue("password1");
        $("[data-testid=submit-button]").click();

        $("[data-testid=welcome-message]").shouldHave(text("Welcome, user1!"));
    }

    @Test
    @Tag("negative")
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
    @DisplayName("Authorization fails with missing password")
    void missinPasswordAuthorizationTest() {
        open("/login.html");

        $("[data-testid=login-input]").setValue("user1");
        $("[data-testid=submit-button]").click();

        $("[data-testid=error-message]").shouldHave(text("Password is required (minimum 6 characters)"));
    }
}
