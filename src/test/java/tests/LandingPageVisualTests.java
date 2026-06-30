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

import static com.codeborne.selenide.Condition.attributeMatching;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static helpers.BrowserSessionHelper.openDemoPage;
import static helpers.NavScreenshot.captureAndCompare;
import static helpers.ViewportHelper.setViewport;

@Layer("e2e")
@Epic("One Page Form")
@Feature("Landing page reference")
@DisplayName("Landing page visual reference")
@Execution(ExecutionMode.SAME_THREAD)
class LandingPageVisualTests extends TestBase {

    private static final Path BASELINE_ROOT =
            Path.of("src/test/resources/screenshots/landing");

    @ParameterizedTest(name = "viewport {0}px")
    @ValueSource(ints = {1280, 768, 390})
    @Tag("reference")
    @Tag("visual")
    @DisplayName("Landing demo section screenshot matches reference")
    void landingDemoSectionMatchesReference(int viewportWidth) {
        setViewport(viewportWidth, 900);
        openDemoPage("/index.html");
        prepareStaticLandingView();

        captureAndCompare(
                $(".card-body-demo"),
                BASELINE_ROOT.resolve("demo-section").resolve(viewportWidth + ".png"),
                "Landing demo section %dpx".formatted(viewportWidth));
    }

    @ParameterizedTest(name = "viewport {0}px")
    @ValueSource(ints = {1280, 768, 390})
    @Tag("reference")
    @Tag("visual")
    @DisplayName("Landing metrics block screenshot matches reference")
    void landingMetricsBlockMatchesReference(int viewportWidth) {
        setViewport(viewportWidth, 900);
        openDemoPage("/index.html");
        prepareStaticLandingView();

        captureAndCompare(
                landingMetricsTarget(),
                BASELINE_ROOT.resolve("metrics-block").resolve(viewportWidth + ".png"),
                "Landing metrics block %dpx".formatted(viewportWidth));
    }

    private static void prepareStaticLandingView() {
        $("#dashboard-frame").should(attributeMatching("data-dashboard-ready", "true"));
        executeJavaScript("window.AllureShell?.syncDashboardLayouts?.()");
        executeJavaScript("""
                document.documentElement.dataset.shellLayout = 'wide';
                window.scrollTo(0, 0);
                const frame = document.getElementById('dashboard-frame');
                if (frame) {
                  frame.style.visibility = 'hidden';
                  frame.style.height = '0';
                  frame.style.minHeight = '0';
                }
                """);
    }

    private static SelenideElement landingMetricsTarget() {
        return $(".metrics-panel");
    }
}
