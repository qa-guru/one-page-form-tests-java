package tests;

import annotations.Layer;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static helpers.BrowserSessionHelper.openDemoPage;
import static helpers.HeaderControls.TOOL_COLOR_SELECTORS;
import static helpers.HeaderControls.assertExternalLinksHaveHref;
import static helpers.HeaderControls.assertLangMenuDoesNotJump;
import static helpers.HeaderControls.assertNavToolsColorsStable;
import static helpers.HeaderControls.assertThemeToggleChangesPageTheme;
import static helpers.HeaderControls.readColors;
import static helpers.ViewportHelper.setViewport;

@Layer("e2e")
@Epic("One Page Form")
@Feature("Header controls")
@DisplayName("Header controls tests")
@Execution(ExecutionMode.SAME_THREAD)
class HeaderControlsTests extends TestBase {

    private static final int VIEWPORT_WIDTH = 1280;
    private static Map<String, Map<String, Object>> referenceColors;

    @BeforeAll
    static void captureReferenceColors() {
        setViewport(VIEWPORT_WIDTH, 900);
        openDemoPage("/index.html");
        referenceColors = readColors(TOOL_COLOR_SELECTORS);
    }

    @Test
    @Tag("layout")
    @DisplayName("External header links have href attribute")
    void externalLinksHaveHref() {
        setViewport(VIEWPORT_WIDTH, 900);
        openDemoPage("/index.html");
        assertExternalLinksHaveHref();
    }

    @Test
    @Tag("layout")
    @DisplayName("Theme toggle switches data-theme and page background")
    void themeToggleChangesBackground() {
        setViewport(VIEWPORT_WIDTH, 900);
        openDemoPage("/index.html");
        assertThemeToggleChangesPageTheme();
    }

    @Test
    @Tag("i18n")
    @DisplayName("Lang menu opens without shifting toggle or menu position")
    void langMenuStable() {
        setViewport(VIEWPORT_WIDTH, 900);
        openDemoPage("/index.html");
        assertLangMenuDoesNotJump();
    }

    @Test
    @Tag("layout")
    @DisplayName("Stable header colors do not drift on another page")
    void headerColorsStableAcrossPages() {
        setViewport(VIEWPORT_WIDTH, 900);
        openDemoPage("/login.html");
        var actual = readColors(TOOL_COLOR_SELECTORS);
        assertNavToolsColorsStable(referenceColors, actual, "login.html");
    }

    @Test
    @Tag("i18n")
    @DisplayName("RU locale updates lang label")
    void langSwitchUpdatesLabel() {
        setViewport(VIEWPORT_WIDTH, 900);
        openDemoPage("/index.html?ru");

        var label = $("#lang-label").getText();
        org.junit.jupiter.api.Assertions.assertEquals("Рус", label, "lang label should reflect RU locale");
    }
}
