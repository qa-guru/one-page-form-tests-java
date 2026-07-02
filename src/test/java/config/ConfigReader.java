package config;

import org.aeonbits.owner.ConfigFactory;


public class ConfigReader {
    public static final TestConfig testConfig = ConfigFactory.create(TestConfig.class);
}
