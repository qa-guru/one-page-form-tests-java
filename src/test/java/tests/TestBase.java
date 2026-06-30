package tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;

import allure.Attachments;
import config.ConfigReader;
import config.TestConfig;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Map;

import static com.codeborne.selenide.Selenide.closeWebDriver;
import static config.TestConfig.*;
import org.aeonbits.owner.ConfigFactory;


public class TestBase {

    private static final TestConfig config = ConfigReader.driverConfig;
    
    @BeforeAll
    static void setup() {
        SelenideLogger.addListener("AllureSelenide",
                new AllureSelenide()
                        .screenshots(false)
                        .savePageSource(false));

        Configuration.baseUrl = config.baseUrl();
        Configuration.browser = config.browser();
        Configuration.browserVersion = config.browserVersion();
        Configuration.browserSize = config.browserSize();
        Configuration.headless = config.headless();

        if (!config.remoteUrl().isBlank()) {
            Configuration.remote = config.remoteUrl();
            var capabilities = new MutableCapabilities();
            capabilities.setCapability("selenoid:options", Map.of(
                    "enableVNC", config.enableVnc(),
                    "enableVideo", config.enableVideo(),
                    "enableHar", config.enableHar(),
                    "headless", config.headless()
            ));
            Configuration.browserCapabilities = capabilities;
        } else if (config.headless()) {
            Configuration.browserCapabilities = new ChromeOptions()
                    .addArguments("--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
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

        if (config.attachHarLogs()) {
            Attachments.harLogs();
        }

        if (config.attachLastScreenshot()) {
            Attachments.screenshot("Last screenshot");
        }

        if (config.enableVideo()) {
            Attachments.video();
        }
        if (config.closeBrowserAfterEach()) {
            closeWebDriver();
        }   
    }
}
