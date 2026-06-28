package helpers;

import com.codeborne.selenide.WebDriverRunner;

import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.switchTo;

public final class BrowserSessionHelper {

    private BrowserSessionHelper() {
    }

    public static void resetPageState() {
        if (!WebDriverRunner.hasWebDriverStarted()) {
            return;
        }

        try {
            switchTo().defaultContent();
        } catch (RuntimeException ignored) {
            // not inside a frame
        }

        executeJavaScript("try { localStorage.clear(); sessionStorage.clear(); } catch (e) {}");
        WebDriverRunner.getWebDriver().manage().deleteAllCookies();
    }

    /** Opens a demo page, reusing the current tab and clearing storage when a session already exists. */
    public static void openDemoPage(String path) {
        var normalized = path.startsWith("/") ? path : "/" + path;
        if (WebDriverRunner.hasWebDriverStarted()) {
            resetPageState();
        }
        open(normalized);
    }
}
