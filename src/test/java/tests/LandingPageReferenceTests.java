package tests;

import annotations.Layer;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static helpers.BrowserSessionHelper.openDemoPage;
import static helpers.LandingReference.computedStylesFor;
import static helpers.LandingReference.headerHeightPx;
import static helpers.LandingReference.requiredTestIds;
import static helpers.LandingReference.viewportLayout;
import static helpers.LayoutCss.gridColumnCount;
import static helpers.ViewportHelper.setViewport;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Layer("e2e")
@Epic("One Page Form")
@Feature("Landing page reference")
@DisplayName("Landing page reference")
class LandingPageReferenceTests extends TestBase {

    @Test
    @Tag("reference")
    @DisplayName("index.html keeps required test ids from reference")
    void requiredTestIdsArePresent() {
        openDemoPage("/index.html");

        for (var testId : requiredTestIds()) {
            $("[data-testid='%s']".formatted(testId)).should(exist);
        }
    }

    @Test
    @Tag("reference")
    @DisplayName("Header surface color matches reference")
    void headerSurfaceColorMatchesReference() {
        openDemoPage("/index.html");

        var expected = computedStylesFor(".header").get("backgroundColor");
        var actual = executeJavaScript(
                "return getComputedStyle(document.querySelector('.header')).backgroundColor");

        assertEquals(expected, actual, "Header background must match reference rgb value");
    }

    @Test
    @Tag("reference")
    @DisplayName("Header height matches reference")
    void headerHeightMatchesReference() {
        openDemoPage("/index.html");

        var actual = executeJavaScript(
                "const el = document.querySelector('.header');"
                        + "return Math.round(el.getBoundingClientRect().height * 100) / 100");

        assertEquals(headerHeightPx(), ((Number) actual).doubleValue(), 0.6,
                "Header height must stay at reference value");
    }

    @ParameterizedTest(name = "viewport {0}px")
    @CsvSource({
            "1280",
            "769",
            "768",
            "390"
    })
    @Tag("reference")
    @Tag("layout")
    @DisplayName("Landing layout matches reference viewports")
    void landingLayoutMatchesReference(int viewportWidth) {
        setViewport(viewportWidth, 900);
        openDemoPage("/index.html");
        $("#dashboard-frame").should(exist);
        executeJavaScript("window.AllureShell?.syncDashboardLayouts?.()");

        var expected = viewportLayout().get(String.valueOf(viewportWidth));
        var demoColumns = gridColumnCount(readDemoListGridTemplate());
        var dashboardColumns = gridColumnCount(readDashboardGridTemplate());

        assertEquals(expected.get("demoColumns"), demoColumns,
                "demo-list columns at %dpx".formatted(viewportWidth));
        assertEquals(expected.get("dashboardColumns"), dashboardColumns,
                "dashboard columns at %dpx".formatted(viewportWidth));
    }

    private static String readDemoListGridTemplate() {
        return executeJavaScript("""
                const list = document.querySelector('.demo-list');
                return list ? getComputedStyle(list).gridTemplateColumns : '';
                """);
    }

    private static String readDashboardGridTemplate() {
        com.codeborne.selenide.Selenide.switchTo().frame($("#dashboard-frame"));
        try {
            return executeJavaScript("""
                    const deadline = Date.now() + 15000;
                    return new Promise((resolve, reject) => {
                      const read = () => {
                        const grid = document.querySelector('[class*="styles_grid__"]');
                        if (grid) {
                          resolve(getComputedStyle(grid).gridTemplateColumns);
                          return;
                        }
                        if (Date.now() > deadline) {
                          reject(new Error('Allure dashboard grid not found'));
                          return;
                        }
                        requestAnimationFrame(read);
                      };
                      read();
                    });
                    """);
        } finally {
            com.codeborne.selenide.Selenide.switchTo().defaultContent();
        }
    }
}
