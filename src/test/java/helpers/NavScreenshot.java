package helpers;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Attachment;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class NavScreenshot {

    private static final double MAX_DIFF_RATIO = 0.015;

    private NavScreenshot() {
    }

    public static void captureAndCompare(SelenideElement element, Path baselinePath, String attachmentName) {
        var screenshotFile = element.screenshot();
        byte[] actual;
        try {
            actual = Files.readAllBytes(screenshotFile.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        attachPng(attachmentName, actual);

        if (shouldUpdateBaselines() || !Files.exists(baselinePath)) {
            writeBaseline(baselinePath, actual);
            return;
        }

        try {
            var expected = Files.readAllBytes(baselinePath);
            assertImagesSimilar(expected, actual, baselinePath.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Attachment(value = "{0}", type = "image/png")
    private static byte[] attachPng(String name, byte[] png) {
        return png;
    }

    private static boolean shouldUpdateBaselines() {
        return Boolean.parseBoolean(System.getProperty("updateNavBaselines", "false"));
    }

    private static void writeBaseline(Path baselinePath, byte[] png) {
        try {
            Files.createDirectories(baselinePath.getParent());
            Files.write(baselinePath, png);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void assertImagesSimilar(byte[] expectedBytes, byte[] actualBytes, String label)
            throws IOException {
        var expected = readImage(expectedBytes);
        var actual = readImage(actualBytes);

        if (expected.getWidth() != actual.getWidth() || expected.getHeight() != actual.getHeight()) {
            var diffPath = Path.of("build", "nav-screenshot-diff", label.replace('/', '_') + "-actual.png");
            Files.createDirectories(diffPath.getParent());
            Files.write(diffPath, actualBytes);
            throw new AssertionError(
                    "Screenshot size changed for %s: expected %dx%d, actual %dx%d. Actual saved to %s"
                            .formatted(
                                    label,
                                    expected.getWidth(),
                                    expected.getHeight(),
                                    actual.getWidth(),
                                    actual.getHeight(),
                                    diffPath));
        }

        var width = expected.getWidth();
        var height = expected.getHeight();
        var diffPixels = 0;
        var totalPixels = width * height;

        for (var y = 0; y < height; y++) {
            for (var x = 0; x < width; x++) {
                if (expected.getRGB(x, y) != actual.getRGB(x, y)) {
                    diffPixels++;
                }
            }
        }

        var diffRatio = (double) diffPixels / totalPixels;
        if (diffRatio > MAX_DIFF_RATIO) {
            var diffPath = Path.of("build", "nav-screenshot-diff", label.replace('/', '_') + "-actual.png");
            Files.createDirectories(diffPath.getParent());
            Files.copy(new ByteArrayInputStream(actualBytes), diffPath, StandardCopyOption.REPLACE_EXISTING);
            throw new AssertionError(
                    "Screenshot diff too high for %s: %.2f%% > %.2f%%. Actual saved to %s"
                            .formatted(label, diffRatio * 100, MAX_DIFF_RATIO * 100, diffPath));
        }
    }

    private static BufferedImage readImage(byte[] bytes) throws IOException {
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            var image = ImageIO.read(in);
            if (image == null) {
                throw new IOException("Unsupported screenshot format");
            }
            return image;
        }
    }
}
