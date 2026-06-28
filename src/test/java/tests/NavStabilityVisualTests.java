package tests;

import annotations.Layer;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static helpers.BrowserSessionHelper.openDemoPage;
import static helpers.NavLayout.STABLE_HEADER_SELECTORS;
import static helpers.NavLayout.assertNavLinkWeightIsMedium;
import static helpers.NavLayout.assertPositionsMatch;
import static helpers.NavLayout.activeSelectorForPage;
import static helpers.NavLayout.readPositions;
import static helpers.NavLayout.selectorsForPage;
import static helpers.NavScreenshot.captureAndCompare;
import static helpers.ViewportHelper.setViewport;

@Layer("e2e")
@Epic("One Page Form")
@Feature("Navigation stability")
@DisplayName("Navigation stability visual tests")
@Execution(ExecutionMode.SAME_THREAD)
class NavStabilityVisualTests extends ReuseBrowserTestBase {

    private static final int VIEWPORT_WIDTH = 1280;
    private static final Path BASELINE_ROOT = Path.of("src/test/resources/screenshots/nav-stability");
    private static Map<String, Map<String, Object>> referencePositions;
    private static Map<String, Map<String, Object>> referenceStablePositions;
    private static Map<String, Map<String, Object>> referenceStablePositionsRu;

    @BeforeAll
    static void captureReferenceLayout() {
        setViewport(VIEWPORT_WIDTH, 900);
        openDemoPage("/index.html");
        referencePositions = readPositions(selectorsForPage("index.html"));
        referenceStablePositions = readPositions(STABLE_HEADER_SELECTORS);

        openDemoPage("/index.html?ru");
        referenceStablePositionsRu = readPositions(STABLE_HEADER_SELECTORS);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "index.html",
            "text-box.html",
            "automation-practice-form.html",
            "login.html",
            "sandbox.html"
    })
    @Tag("layout")
    @Tag("visual")
    @DisplayName("Header nav stays stable across demo pages")
    void headerNavStaysStableOnPage(String pagePath) {
        setViewport(VIEWPORT_WIDTH, 900);
        openDemoPage("/" + pagePath);

        var positions = readPositions(selectorsForPage(pagePath));
        assertPositionsMatch(referencePositions, positions, pagePath);

        var activeSelector = activeSelectorForPage(pagePath);
        var activePositions = readPositions(List.of(activeSelector));
        assertNavLinkWeightIsMedium(activeSelector, activePositions);

        captureAndCompare(
                $(".header-left"),
                BASELINE_ROOT.resolve("header-left").resolve(pagePath.replace(".html", "") + ".png"),
                "Header left on %s".formatted(pagePath));

        captureAndCompare(
                $("[data-testid='nav-tools']"),
                BASELINE_ROOT.resolve("header-right").resolve("nav-tools.png"),
                "Header tools on %s".formatted(pagePath));
    }

    @ParameterizedTest(name = "{0}?ru stable selectors")
    @CsvSource({
            "index.html",
            "text-box.html",
            "automation-practice-form.html",
            "login.html",
            "sandbox.html"
    })
    @Tag("layout")
    @Tag("i18n")
    @DisplayName("Stable header controls stay aligned at RU locale")
    void headerStableSelectorsAtRu(String pagePath) {
        setViewport(VIEWPORT_WIDTH, 900);
        openDemoPage("/" + pagePath + "?ru");

        var positions = readPositions(STABLE_HEADER_SELECTORS);
        assertPositionsMatch(referenceStablePositionsRu, positions, pagePath + "?ru");
        assertNavLinkWeightIsMedium("[data-testid='forms-link']", positions);
    }

    @ParameterizedTest(name = "viewport {0}px")
    @CsvSource({
            "1920",
            "1280",
            "1150",
            "1024"
    })
    @Tag("layout")
    @Tag("visual")
    @DisplayName("Header tools screenshot stays stable at desktop widths")
    void headerToolsScreenshotAtWidths(int viewportWidth) {
        setViewport(viewportWidth, 900);
        openDemoPage("/login.html");

        captureAndCompare(
                $("[data-testid='nav-tools']"),
                BASELINE_ROOT.resolve("header-right").resolve(viewportWidth + ".png"),
                "Header tools %dpx".formatted(viewportWidth));
    }
}
