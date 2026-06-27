package config;

import java.nio.file.Paths;

import org.aeonbits.owner.ConfigFactory;

public class ConfigReader {

  public static final TestConfig config = ConfigFactory.create(TestConfig.class);

  public static String resolvedBaseUrl() {
    String baseUrl = config.baseUrlRaw();
    if (baseUrl != null && !baseUrl.isBlank()) {
      return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    var siblingDemo = Paths.get("../one-page-form/").toAbsolutePath().normalize();
    if (siblingDemo.resolve("login.html").toFile().exists()) {
      return siblingDemo.toUri().toString();
    }

    return Paths.get("../").toAbsolutePath().normalize().toUri().toString();
  }
}
