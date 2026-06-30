package helpers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Landing reference assets")
class LandingReferenceAssetsTest {

    private static final Pattern ROOT_BLOCK =
            Pattern.compile(":root\\s*\\{([^}]+)\\}", Pattern.DOTALL);
    private static final Pattern ROOT_TOKEN =
            Pattern.compile("(--[\\w-]+)\\s*:\\s*([^;]+);");

    @Test
    @Tag("reference")
    @DisplayName("shell CSS tokens match landing reference")
    void cssTokensMatchReferenceFile() throws Exception {
        var reference = LandingReference.cssTokens();
        var actual = mergedShellTokens();

        for (var entry : reference.entrySet()) {
            assertTrue(actual.containsKey(entry.getKey()),
                    "Missing CSS token in shell CSS: " + entry.getKey());
            assertEquals(entry.getValue(), actual.get(entry.getKey()),
                    "CSS token drift for " + entry.getKey());
        }
    }

    private static Map<String, String> mergedShellTokens() throws Exception {
        var tokens = new LinkedHashMap<String, String>();
        var headerPath = resolveDemoAsset("qa-guru-header.css");
        if (Files.isRegularFile(headerPath)) {
            tokens.putAll(parseRootTokens(Files.readString(headerPath)));
        }
        tokens.putAll(parseRootTokens(Files.readString(resolveDemoAsset("allure-shell.css"))));
        return tokens;
    }

    private static Path resolveDemoAsset(String fileName) {
        var ciDemo = Path.of("demo", fileName);
        if (Files.isRegularFile(ciDemo)) {
            return ciDemo;
        }
        var sibling = Path.of("..", "one-page-form", fileName).normalize().toAbsolutePath();
        if (Files.isRegularFile(sibling)) {
            return sibling;
        }
        throw new IllegalStateException("Demo asset not found: " + fileName);
    }

    private static Map<String, String> parseRootTokens(String css) {
        var match = ROOT_BLOCK.matcher(css);
        if (!match.find()) {
            throw new IllegalArgumentException(":root block not found");
        }
        var tokens = new LinkedHashMap<String, String>();
        Matcher tokenMatcher = ROOT_TOKEN.matcher(match.group(1));
        while (tokenMatcher.find()) {
            tokens.put(tokenMatcher.group(1), tokenMatcher.group(2).trim());
        }
        return tokens;
    }
}
