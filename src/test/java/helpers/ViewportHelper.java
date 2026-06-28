package helpers;

import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chromium.ChromiumDriver;

import java.util.Map;

import static com.codeborne.selenide.Selenide.open;

public final class ViewportHelper {

    private ViewportHelper() {
    }

    public static void setViewport(int width, int height) {
        if (!WebDriverRunner.hasWebDriverStarted()) {
            open("about:blank");
        }

        var driver = WebDriverRunner.getWebDriver();
        if (driver instanceof ChromiumDriver chromium) {
            chromium.executeCdpCommand("Emulation.setDeviceMetricsOverride", Map.of(
                    "width", width,
                    "height", height,
                    "deviceScaleFactor", 1,
                    "mobile", false
            ));
            return;
        }

        driver.manage().window().setSize(new Dimension(width, height));
    }
}
