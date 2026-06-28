package tests;

import org.junit.jupiter.api.AfterAll;

import static com.codeborne.selenide.Selenide.closeWebDriver;

/**
 * Keeps one browser session for the whole test class; storage is reset between navigations
 * via {@link helpers.BrowserSessionHelper#openDemoPage(String)}.
 */
public abstract class ReuseBrowserTestBase extends TestBase {

    @Override
    protected boolean closeBrowserAfterEach() {
        return false;
    }

    @AfterAll
    static void closeSharedBrowser() {
        closeWebDriver();
    }
}
