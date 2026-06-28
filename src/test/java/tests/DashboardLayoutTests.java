package tests;

import annotations.Layer;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.codeborne.selenide.Condition.attributeMatching;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static helpers.BrowserSessionHelper.openDemoPage;
import static com.codeborne.selenide.Selenide.switchTo;
import static helpers.LayoutCss.RESPONSIVE_BREAKPOINT_PX;
import static helpers.LayoutCss.gridColumnCount;
import static helpers.ViewportHelper.setViewport;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Layer("e2e")
@Epic("One Page Form")
@Feature("Dashboard layout")
@DisplayName("Dashboard layout")
@Execution(ExecutionMode.SAME_THREAD)
class DashboardLayoutTests extends ReuseBrowserTestBase {

    private static final String DEMO_LIST_GRID_SCRIPT = """
            const list = document.querySelector('.demo-list');
            return list ? getComputedStyle(list).gridTemplateColumns : '';
            """;

    @ParameterizedTest(name = "viewport {0}px: demo {1} col, dashboard {2} col")
    @CsvSource({
            "1280, 3, 2",
            "769, 3, 2",
            "768, 1, 1",
            "390, 1, 1"
    })
    @Tag("layout")
    @DisplayName("Demo cards and Allure dashboard share responsive breakpoint")
    void demoAndDashboardColumnsStayInSync(int viewportWidth, int expectedDemoColumns, int expectedDashboardColumns) {
        setViewport(viewportWidth, 900);
        openLandingWithDashboard();

        var demoColumns = gridColumnCount(readDemoListGridTemplate());
        var dashboardColumns = gridColumnCount(readDashboardGridTemplate());

        assertEquals(expectedDemoColumns, demoColumns,
                "Unexpected demo-list column count at %dpx viewport".formatted(viewportWidth));
        assertEquals(expectedDashboardColumns, dashboardColumns,
                "Unexpected Allure dashboard column count at %dpx viewport".formatted(viewportWidth));

        assertEquals(demoColumns > 1, dashboardColumns > 1,
                "Demo (%d col) and dashboard (%d col) must switch layout together at %dpx"
                        .formatted(demoColumns, dashboardColumns, viewportWidth));
    }

    @ParameterizedTest(name = "breakpoint edge {0}px")
    @CsvSource({
            "769",
            "768"
    })
    @Tag("layout")
    @DisplayName("Layout switches around shared 768px breakpoint")
    void layoutSwitchesAroundSharedBreakpoint(int viewportWidth) {
        setViewport(viewportWidth, 900);
        openLandingWithDashboard();

        var demoColumns = gridColumnCount(readDemoListGridTemplate());
        var dashboardColumns = gridColumnCount(readDashboardGridTemplate());
        var expectedMultiColumn = viewportWidth > RESPONSIVE_BREAKPOINT_PX;

        assertEquals(expectedMultiColumn, demoColumns > 1,
                "demo-list should be %s at %dpx".formatted(columnMode(expectedMultiColumn), viewportWidth));
        assertEquals(expectedMultiColumn, dashboardColumns > 1,
                "dashboard should be %s at %dpx".formatted(columnMode(expectedMultiColumn), viewportWidth));
    }

    private static void openLandingWithDashboard() {
        openDemoPage("/index.html");
        $("#dashboard-frame").should(attributeMatching("data-dashboard-ready", "true"));
        executeJavaScript("window.AllureShell?.syncDashboardLayouts?.()");
    }

    private static String readDemoListGridTemplate() {
        return executeJavaScript(DEMO_LIST_GRID_SCRIPT);
    }

    private static String readDashboardGridTemplate() {
        switchTo().frame($("#dashboard-frame"));
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
            switchTo().defaultContent();
        }
    }

    private static String columnMode(boolean multiColumn) {
        return multiColumn ? "multi-column" : "single-column";
    }
}
