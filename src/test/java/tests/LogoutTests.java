package tests;

import annotations.Layer;
import static io.qameta.allure.Allure.step;
import io.qameta.allure.Severity;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.SeverityLevel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Layer("e2e")
@Epic("One Page Form")
@Feature("Login")
@DisplayName("Logout")
public class LogoutTests extends TestBase {

    
    @Test
    @Tag("smoke")
    @Tag("positive")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Successful logout with login form submission")
    @Description("Bad practice: Legacy E2E UI Test. Should be splitted to separate tests - successful login, successful logout")
    void successfulLogoutTest() {
        loginPage.openPage();
        loginPage.fillAndSubmitForm("user1", "password1");
        logedInPage.shouldHaveWelcomeMessage("Welcome, user1!");
        logedInPage.clickLogoutButton();
        loginPage.shouldHaveFormTitle("Login");
    }

    @Test
    @Tag("smoke")
    @Tag("positive")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Successful logout with local storage authentication")
    void successfulLogoutWithLocalStorageAuthenticationTest() {
        logedInPage.openPageWithLocalStorageAuthentication("user1", "password1")
                .shouldHaveWelcomeMessage("Welcome, user1!")
                .clickLogoutButton()
                .shouldHaveFormTitle("Login");
    }
}
