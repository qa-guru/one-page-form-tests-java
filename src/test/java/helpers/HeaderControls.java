package helpers;

import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import com.codeborne.selenide.Condition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class HeaderControls {

    public static final String MEASURE_COLORS_SCRIPT = """
            const selectors = arguments[0];
            const result = {};
            selectors.forEach((selector) => {
              const el = document.querySelector(selector);
              if (!el) return;
              const style = getComputedStyle(el);
              result[selector] = {
                color: style.color,
                backgroundColor: style.backgroundColor
              };
            });
            return result;
            """;

    public static final String PAGE_BG_SCRIPT = """
            return getComputedStyle(document.body).backgroundColor;
            """;

    public static final List<String> TOOL_COLOR_SELECTORS = List.of(
            "[data-testid='lang-toggle']",
            "[data-testid='theme-toggle']",
            "[data-testid='github-link']",
            "[data-testid='github-io-link']"
    );

    public static final String LANG_MENU_RECT_SCRIPT = """
            const menu = document.querySelector('#lang-menu');
            if (!menu) return null;
            const rect = menu.getBoundingClientRect();
            return { left: rect.left, top: rect.top, width: rect.width, height: rect.height };
            """;

    private HeaderControls() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, Object>> readColors(List<String> selectors) {
        return executeJavaScript(MEASURE_COLORS_SCRIPT, selectors);
    }

    public static void assertExternalLinksHaveHref() {
        for (var selector : List.of(
                "[data-testid='logo-link']",
                "[data-testid='github-link']",
                "[data-testid='github-io-link']"
        )) {
            var href = $(selector).getAttribute("href");
            assertNotNull(href, selector + " must have href");
            assertFalse(href.isBlank(), selector + " href must not be empty");
        }
    }

    public static void assertThemeToggleChangesPageTheme() {
        var html = $("html");
        var themeBefore = html.getAttribute("data-theme");
        var bgBefore = pageBackground();

        $("[data-testid='theme-toggle']").click();
        var themeAfter = html.getAttribute("data-theme");
        var bgAfter = pageBackground();

        assertNotEquals(themeBefore, themeAfter, "data-theme must change after theme toggle");
        assertNotEquals(bgBefore, bgAfter, "page background must change with theme");

        $("[data-testid='theme-toggle']").click();
        assertEquals(themeBefore, html.getAttribute("data-theme"));
    }

    @SuppressWarnings("unchecked")
    public static void assertLangMenuDoesNotJump() {
        var toggle = $("[data-testid='lang-toggle']");
        var toggleBefore = NavLayout.readPositions(List.of("[data-testid='lang-toggle']"));

        toggle.click();
        assertTrue($("#lang-menu").has(Condition.cssClass("is-open")), "lang menu must open");

        var menuRect = (Map<String, Number>) executeJavaScript(LANG_MENU_RECT_SCRIPT);
        assertNotNull(menuRect, "lang menu rect");
        var toggleRect = toggle.getRect();
        assertTrue(menuRect.get("top").doubleValue() >= toggleRect.y + toggleRect.height - 2,
                "lang menu should open below toggle");

        var toggleOpen = NavLayout.readPositions(List.of("[data-testid='lang-toggle']"));
        NavLayout.assertPositionsMatch(
                toggleBefore,
                toggleOpen,
                "lang menu open"
        );

        var menuRectFirst = menuRect;
        com.codeborne.selenide.Selenide.actions().sendKeys(org.openqa.selenium.Keys.ESCAPE).perform();
        assertFalse($("#lang-menu").has(Condition.cssClass("is-open")), "lang menu must close");

        toggle.click();
        var menuRectSecond = (Map<String, Number>) executeJavaScript(LANG_MENU_RECT_SCRIPT);
        assertNotNull(menuRectSecond);

        assertNear(menuRectFirst.get("left"), menuRectSecond.get("left"), "lang menu left");
        assertNear(menuRectFirst.get("top"), menuRectSecond.get("top"), "lang menu top");
    }

    public static void assertNavToolsColorsStable(
            Map<String, Map<String, Object>> reference,
            Map<String, Map<String, Object>> actual,
            String context
    ) {
        for (var entry : actual.entrySet()) {
            var selector = entry.getKey();
            var expected = reference.get(selector);
            assertNotNull(expected, "no color reference for " + selector + " (" + context + ")");
            assertEquals(expected.get("color"), entry.getValue().get("color"),
                    "icon/link color changed for " + selector + " on " + context);
        }
    }

    private static String pageBackground() {
        Object value = executeJavaScript(PAGE_BG_SCRIPT);
        return value == null ? "" : value.toString();
    }

    private static void assertNear(Number expected, Number actual, String label) {
        if (Math.abs(expected.doubleValue() - actual.doubleValue()) > 2) {
            throw new AssertionError("%s drifted: expected %.2f, actual %.2f"
                    .formatted(label, expected.doubleValue(), actual.doubleValue()));
        }
    }

}
