package helpers;

import java.util.List;

public final class NavLayout {

    public static final int EXPECTED_GAP_PX = 12;
    public static final double GAP_TOLERANCE_PX = 0.6;

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
}
