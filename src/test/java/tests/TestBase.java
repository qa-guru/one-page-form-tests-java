package tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;

import allure.Attachments;
import config.ConfigReader;
import config.TestConfig;
import pages.LoginPage;
import pages.LogedInPage;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.chrome.ChromeOptions;

import static com.codeborne.selenide.Selenide.closeWebDriver;


public class TestBase {

    LoginPage loginPage = new LoginPage();  
    LogedInPage logedInPage = new LogedInPage();
    
    protected static final TestConfig config = ConfigReader.testConfig;
    
    @BeforeAll
    static void setup() {
        Configuration.baseUrl = config.baseUrl();
        Configuration.browser = config.browser();
        Configuration.browserVersion = config.browserVersion();
        Configuration.browserSize = config.browserSize();
        Configuration.headless = config.headless();

        if (!config.remoteUrl().isBlank()) {
            Configuration.remote = config.remoteUrl();
        }

        if (config.headless()) {
            Configuration.browserCapabilities = new ChromeOptions()
                    .addArguments("--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
        }

        if (config.enableAllureSelenideStepsListener()) {
            SelenideLogger.addListener("AllureSelenide",
                new AllureSelenide()
                        .screenshots(false)
                        .savePageSource(false));
        }
    }

    @AfterEach
    void afterEach() {
        if (config.attachBrowserConsoleLogs()) {
            Attachments.browserConsoleLogs();
        }
        if (config.attachPageSource()) {
            Attachments.pageSource();
        }

        if (config.attachLastScreenshot()) {
            Attachments.screenshot("Last screenshot");
        }

        if (config.closeBrowserAfterEach()) {
            closeWebDriver();
        }   
    }
}
