package config;

import org.aeonbits.owner.Config;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:properties",
        "classpath:config/${env}.properties",
})
public interface TestConfig extends Config {

    @Key("attachBrowserConsoleLogs")
    @DefaultValue("false")
    boolean attachBrowserConsoleLogs();
    
    @Key("attachHarLogs")
    @DefaultValue("false")
    boolean attachHarLogs();

    @Key("attachLastScreenshot")
    @DefaultValue("false")
    boolean attachLastScreenshot();

    @Key("attachPageSource")
    @DefaultValue("false")
    boolean attachPageSource();

    @Key("attachVideo")
    @DefaultValue("false")
    boolean attachVideo();

    @Key("baseUrl")
    String baseUrl();

    @Key("basePath")
    @DefaultValue("")
    String basePath();

    @Key("browser")
    @DefaultValue("chrome")
    String browser();

    @Key("browserSize")
    @DefaultValue("1920x1280")
    String browserSize();

    @Key("browserVersion")
    @DefaultValue("148")
    String browserVersion();

    @Key("closeBrowserAfterEach")
    @DefaultValue("false")
    boolean closeBrowserAfterEach();

    @Key("enableAllureSelenideStepsListener")
    @DefaultValue("false")
    boolean enableAllureSelenideStepsListener();

    @Key("enableHar")
    @DefaultValue("false")
    boolean enableHar();

    @Key("enableVnc")
    @DefaultValue("false")
    boolean enableVnc();

    @Key("enableVideo")
    @DefaultValue("false")
    boolean enableVideo();

    @Key("headless")
    @DefaultValue("false")
    boolean headless();

    @Key("videoFolder")
    @DefaultValue("")
    String videoFolder();

    @Key("remoteUrl")
    @DefaultValue("")
    String remoteUrl();

}
