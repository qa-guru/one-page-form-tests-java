package helpers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class NavLayout {

    public static final int EXPECTED_GAP_PX = 12;
    public static final double GAP_TOLERANCE_PX = 0.6;
    public static final double POSITION_TOLERANCE_PX = 0.6;

    public static final List<String> STABLE_HEADER_SELECTORS = List.of(
            "[data-testid='logo-link']",
            "[data-testid='forms-link']",
            "[data-testid='lang-toggle']",
            "[data-testid='theme-toggle']",
            "[data-testid='github-link']",
            "[data-testid='github-io-link']"
    );

    public static final List<String> FULL_NAV_SELECTORS = List.of(
            "[data-testid='clubs-link']",
            "[data-testid='create-club-link']",
            "[data-testid='login-link']",
            "[data-testid='sandbox-link']"
    );

    public static final String MEASURE_NAV_POSITIONS_SCRIPT = """
            const selectors = arguments[0];
            const result = {};
            selectors.forEach((selector) => {
              const el = document.querySelector(selector);
              if (!el) return;
              const rect = el.getBoundingClientRect();
              const style = getComputedStyle(el);
              result[selector] = {
                left: Math.round(rect.left * 100) / 100,
                top: Math.round(rect.top * 100) / 100,
                width: Math.round(rect.width * 100) / 100,
                height: Math.round(rect.height * 100) / 100,
                fontWeight: style.fontWeight,
                fontSize: style.fontSize
              };
            });
            return result;
            """;

    public static final String MEASURE_HEADER_GAPS_SCRIPT = """
            const root = document.querySelector('.header-left');
            const nav = root?.querySelector('.form-nav');
            if (!root || !nav) return [];
            const items = [...root.children].filter(el => el !== nav).concat([...nav.children]);
            return items.slice(0, -1).map((el, i) => {
              const a = el.getBoundingClientRect();
              const b = items[i + 1].getBoundingClientRect();
              return Math.round((b.left - a.right) * 100) / 100;
            });
            """;

    public static final String MEASURE_DASHBOARD_GAPS_SCRIPT = """
            const items = [...document.querySelectorAll('.dashboard-nav > *')];
            return items.slice(0, -1).map((el, i) => {
              const a = el.getBoundingClientRect();
              const b = items[i + 1].getBoundingClientRect();
              return Math.round((b.left - a.right) * 100) / 100;
            });
            """;

    private NavLayout() {
    }

    public static void assertUniformGaps(List<? extends Number> gaps, String navName, int viewportWidth) {
        if (gaps.isEmpty()) {
            throw new AssertionError("No gaps measured for %s at %dpx viewport".formatted(navName, viewportWidth));
        }

        var min = gaps.stream().mapToDouble(Number::doubleValue).min().orElseThrow();
        var max = gaps.stream().mapToDouble(Number::doubleValue).max().orElseThrow();

        if (max - min > GAP_TOLERANCE_PX) {
            throw new AssertionError(
                    "%s gaps are uneven at %dpx: min=%.2f max=%.2f values=%s"
                            .formatted(navName, viewportWidth, min, max, gaps));
        }

        var expected = EXPECTED_GAP_PX;
        if (Math.abs(min - expected) > GAP_TOLERANCE_PX || Math.abs(max - expected) > GAP_TOLERANCE_PX) {
            throw new AssertionError(
                    "%s gaps drift from %dpx at %dpx viewport: min=%.2f max=%.2f values=%s"
                            .formatted(navName, expected, viewportWidth, min, max, gaps));
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, Object>> readPositions(List<String> selectors) {
        return com.codeborne.selenide.Selenide.executeJavaScript(
                MEASURE_NAV_POSITIONS_SCRIPT,
                selectors
        );
    }

    public static void assertPositionsMatch(
            Map<String, Map<String, Object>> reference,
            Map<String, Map<String, Object>> actual,
            String pageName
    ) {
        for (var entry : actual.entrySet()) {
            var selector = entry.getKey();
            var current = entry.getValue();
            var expected = reference.get(selector);
            assertTrue(expected != null,
                    "No reference position for %s while checking %s".formatted(selector, pageName));

            assertNear(expected.get("left"), current.get("left"), "left", selector, pageName);
            assertNear(expected.get("top"), current.get("top"), "top", selector, pageName);
            assertNear(expected.get("width"), current.get("width"), "width", selector, pageName);
            assertNear(expected.get("height"), current.get("height"), "height", selector, pageName);
            assertEquals(expected.get("fontSize"), current.get("fontSize"),
                    "font-size changed for %s on %s".formatted(selector, pageName));
            assertEquals(expected.get("fontWeight"), current.get("fontWeight"),
                    "font-weight changed for %s on %s".formatted(selector, pageName));
        }
    }

    public static void assertNavLinkWeightIsMedium(String selector, Map<String, Map<String, Object>> positions) {
        var link = positions.get(selector);
        assertTrue(link != null, "Nav link %s is missing".formatted(selector));
        var weight = String.valueOf(link.get("fontWeight"));
        assertTrue("500".equals(weight),
                "Nav link %s must stay font-weight 500, got %s".formatted(selector, weight));
    }

    public static List<String> selectorsForPage(String pagePath) {
        var selectors = new java.util.ArrayList<>(STABLE_HEADER_SELECTORS);
        if (!"sandbox.html".equals(pagePath)) {
            selectors.addAll(FULL_NAV_SELECTORS);
        }
        return selectors;
    }

    public static String activeSelectorForPage(String pagePath) {
        return switch (pagePath) {
            case "index.html" -> "[data-testid='forms-link']";
            case "text-box.html" -> "[data-testid='clubs-link']";
            case "automation-practice-form.html" -> "[data-testid='create-club-link']";
            case "login.html" -> "[data-testid='login-link']";
            case "sandbox.html" -> "[data-testid='sandbox-link']";
            default -> throw new IllegalArgumentException("Unknown page: " + pagePath);
        };
    }

    private static void assertNear(Object expected, Object actual, String axis, String selector, String pageName) {
        var expectedValue = ((Number) expected).doubleValue();
        var actualValue = ((Number) actual).doubleValue();
        if (Math.abs(expectedValue - actualValue) > POSITION_TOLERANCE_PX) {
            throw new AssertionError(
                    "%s position shifted for %s on %s: expected %.2f, actual %.2f"
                            .formatted(axis, selector, pageName, expectedValue, actualValue));
        }
    }
}
