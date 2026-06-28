package helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public final class LandingReference {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LandingReference() {
    }

    public static Path referenceRoot() {
        var ciDemo = Paths.get("demo", "reference");
        if (Files.isRegularFile(ciDemo.resolve("index.css-tokens.json"))) {
            return ciDemo;
        }

        var sibling = Paths.get("..", "one-page-form", "reference").normalize();
        if (Files.isRegularFile(sibling.resolve("index.css-tokens.json"))) {
            return sibling.toAbsolutePath();
        }

        throw new IllegalStateException(
                "Landing reference not found. Expected demo/reference or ../one-page-form/reference");
    }

    public static Map<String, String> cssTokens() {
        return readJson(referenceRoot().resolve("index.css-tokens.json"), new TypeReference<>() {});
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> structure() {
        return readJson(referenceRoot().resolve("index.structure.json"), Map.class);
    }

    @SuppressWarnings("unchecked")
    public static List<String> requiredTestIds() {
        return (List<String>) structure().get("requiredTestIds");
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, Integer>> viewportLayout() {
        var layout = (Map<String, Object>) structure().get("layout");
        return (Map<String, Map<String, Integer>>) layout.get("viewports");
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> computedStylesFor(String selector) {
        var computed = (Map<String, Map<String, String>>) structure().get("computedStyles");
        return computed.get(selector);
    }

    public static int headerHeightPx() {
        return ((Number) ((Map<?, ?>) structure().get("layout")).get("headerHeightPx")).intValue();
    }

    private static <T> T readJson(Path path, TypeReference<T> type) {
        try {
            return MAPPER.readValue(Files.readString(path), type);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + path, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T readJson(Path path, Class<T> type) {
        try {
            return MAPPER.readValue(Files.readString(path), type);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + path, e);
        }
    }
}
