package tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import helpers.Attachments;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Map;

import static com.codeborne.selenide.Selenide.closeWebDriver;
import static config.ConfigReader.config;
import static config.ConfigReader.resolvedBaseUrl;

public class TestBase {

    @BeforeAll
    static void setup() {
        SelenideLogger.addListener("AllureSelenide",
                new AllureSelenide()
                        .screenshots(attachScreenshots())
                        .savePageSource(false));

        Configuration.baseUrl = resolvedBaseUrl();
        Configuration.browser = config.browser();
        Configuration.browserVersion = config.browserVersion();
        Configuration.browserSize = config.browserSize();
        Configuration.headless = config.headless();

        if (isSelenoidRun()) {
            Configuration.remote = config.remoteUrl();
            var capabilities = new MutableCapabilities();
            capabilities.setCapability("selenoid:options", Map.of(
                    "enableVNC", true,
                    "enableVideo", !config.videoFolder().isBlank()
            ));
            Configuration.browserCapabilities = capabilities;
        } else if (config.headless()) {
            Configuration.browserCapabilities = new ChromeOptions()
                    .addArguments("--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
        }
    }

    protected boolean closeBrowserAfterEach() {
        return true;
    }

    @AfterEach
    void afterEach() {
        if (attachScreenshots()) {
            Attachments.screenshotAs("Last screenshot");
        }
        if (isSelenoidRun() && !config.videoFolder().isBlank()) {
            Attachments.video();
        }
        if (closeBrowserAfterEach()) {
            closeWebDriver();
        }
    }

    protected static boolean isSelenoidRun() {
        return !config.remoteUrl().isBlank();
    }

    /** Local runs always; in CI — only when tests go through Selenoid remote. */
    protected static boolean attachScreenshots() {
        return isSelenoidRun() || !"ci".equals(System.getProperty("env"));
    }
}
