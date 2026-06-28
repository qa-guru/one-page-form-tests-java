package tests;

import annotations.Layer;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.open;
import static helpers.NavLayout.MEASURE_DASHBOARD_GAPS_SCRIPT;
import static helpers.NavLayout.MEASURE_HEADER_GAPS_SCRIPT;
import static helpers.NavLayout.assertUniformGaps;
import static helpers.NavScreenshot.captureAndCompare;
import static helpers.ViewportHelper.setViewport;

@Layer("e2e")
@Epic("One Page Form")
@Feature("Navigation spacing")
@DisplayName("Navigation spacing visual tests")
@Execution(ExecutionMode.SAME_THREAD)
class NavSpacingVisualTests extends TestBase {

    private static final Path BASELINE_ROOT = Path.of("src/test/resources/screenshots/nav");

    @ParameterizedTest(name = "viewport {0}px")
    @ValueSource(ints = {1920, 1440, 1280, 1151, 1150, 1024, 768, 390})
    @Tag("layout")
    @Tag("visual")
    @DisplayName("Header nav keeps fixed gaps and screenshot at multiple widths")
    void headerNavSpacingAndScreenshot(int viewportWidth) {
        setViewport(viewportWidth, 900);
        open("/index.html");

        var gaps = readGaps(MEASURE_HEADER_GAPS_SCRIPT);
        assertUniformGaps(gaps, "Header nav", viewportWidth);

        captureAndCompare(
                headerNavShotTarget(),
                BASELINE_ROOT.resolve("header").resolve(viewportWidth + ".png"),
                "Header nav %dpx".formatted(viewportWidth));
    }

    @ParameterizedTest(name = "viewport {0}px")
    @ValueSource(ints = {1920, 1440, 1280, 1151, 1150, 1024, 768, 390})
    @Tag("layout")
    @Tag("visual")
    @DisplayName("Dashboard nav keeps fixed gaps and screenshot at multiple widths")
    void dashboardNavSpacingAndScreenshot(int viewportWidth) {
        setViewport(viewportWidth, 900);
        open("/index.html");

        var gaps = readGaps(MEASURE_DASHBOARD_GAPS_SCRIPT);
        assertUniformGaps(gaps, "Dashboard nav", viewportWidth);

        captureAndCompare(
                $(".card-header-dashboard"),
                BASELINE_ROOT.resolve("dashboard").resolve(viewportWidth + ".png"),
                "Dashboard nav %dpx".formatted(viewportWidth));
    }

    private static SelenideElement headerNavShotTarget() {
        return $(".header-left");
    }

    @SuppressWarnings("unchecked")
    private static List<? extends Number> readGaps(String script) {
        return executeJavaScript(script);
    }
}
