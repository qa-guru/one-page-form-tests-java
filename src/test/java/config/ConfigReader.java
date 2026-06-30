package config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:properties",
        "classpath:config/${env}.properties"
})
public class ConfigReader {
    public static final TestConfig driverConfig = ConfigFactory.create(TestConfig.class);
}
