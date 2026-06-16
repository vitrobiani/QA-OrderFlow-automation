package testCases;

import java.time.Duration;
import java.io.File;
import java.util.HashMap;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Course-style base class (lower-case name on purpose — matches the course's `base_test_class`).
 * Holds the shared driver initializer and project-wide constants.
 */
public class base_test_class {

    // ---- Project constants (course style: public static final) ----
    public static final String BASE_URL  = "https://nano-flow-order-direct.base44.app";
    public static final String API_BASE  = "https://dummyjson.com";
    public static final int    SUM_CAP   = 10000;   // R2 — order sum cap
    public static final int    WAIT_SECS = 15;     // explicit-wait timeout

    /**
     * Course-standard driver initializer. Selenium 4.16 has Selenium Manager built in,
     * so no chromedriver path is needed. The old course line is kept commented for familiarity.
     */
    public static WebDriver initializeDriver() {
        // Old course style (kept for reference — Selenium Manager replaces it):
        // System.setProperty("webdriver.chrome.driver", "C:\\...\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();

        // Force downloads into ./downloads so Test174 (CSV export) can find the file.
        File dlDir = new File("downloads");
        if (!dlDir.exists()) dlDir.mkdirs();
        HashMap<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", dlDir.getAbsolutePath());
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("safebrowsing.enabled", true);
        options.setExperimentalOption("prefs", prefs);

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize();
        return driver;
    }
}
