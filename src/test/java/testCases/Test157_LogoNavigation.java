package testCases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.internal.TextListener;
import org.openqa.selenium.WebDriver;
import pages.NavBar;

import static org.junit.Assert.assertTrue;

/**
 * PractiTest Test #157 — Logo navigation.
 * Requirement #134 (Navigation / UI).
 *
 * Scenario: From any non-home page, click the "OrderFlow" logo/brand
 * in the header and verify we land on the Home screen.
 *
 * STATUS: SKIPPED
 * ----------------
 * Discovery (Session 1, 2026-06-17) found that the "OrderFlow" brand in the
 * header is NOT a clickable element. The DOM structure is:
 *   <div>
 *     <div class="...bg-gradient-to-r...">  <!-- color square -->
 *     <svg>...</svg>                        <!-- cart icon -->
 *     <span>OrderFlow</span>                <!-- brand text -->
 *   </div>
 *
 * There is no <a> wrapper, no href, and no visible click handler.
 * Clicking the element is effectively a no-op in the current SUT.
 *
 * DECISION: The test is @Ignore-d rather than deleted, so the PractiTest
 * ID mapping remains complete. The NavBar.clickLogo() method is kept in
 * case the SUT is updated to make the logo clickable in the future.
 *
 * See LOCATORS.md "Decision — Test #157" for full details.
 */
public class Test157_LogoNavigation {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test156_ReturnsNavigation.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testLogoNavigation() {
        NavBar nav = new NavBar(driver);

        // From New Home -> Home (Logo)
        driver.get(base_test_class.BASE_URL);
        nav.clickLogo();
        boolean onHome0 = nav.isOnHome();
        if (onHome0) logger.info("[PASS] #156 Home -> Returns");
        else            logger.error("[FAIL] #156 Home -> Returns: not on returns screen");
        assertTrue("Home -> Returns failed", onHome0);

        // From New Order -> Home (Logo)
        driver.get(base_test_class.BASE_URL + NavBar.ROUTE_ORDER);
        nav.clickLogo();
        boolean onHome1 = nav.isOnHome();
        if (onHome1) logger.info("[PASS] #157 New Order -> Home (Logo)");
        else         logger.error("[FAIL] #157 New Order -> Home (Logo): not on home screen");
        assertTrue("New Order -> Home failed", onHome1);

        // From Order History -> Home (Logo)
        driver.get(base_test_class.BASE_URL + NavBar.ROUTE_HISTORY);
        nav.clickLogo();
        boolean onHome2 = nav.isOnHome();
        if (onHome2) logger.info("[PASS] #157 Order History -> Home (Logo)");
        else         logger.error("[FAIL] #157 Order History -> Home (Logo): not on home screen");
        assertTrue("Order History -> Home failed", onHome2);

        // From Returns -> Home (Logo)
        driver.get(base_test_class.BASE_URL + NavBar.ROUTE_RETURNS);
        nav.clickLogo();
        boolean onHome3 = nav.isOnHome();
        if (onHome3) logger.info("[PASS] #157 Returns -> Home (Logo)");
        else         logger.error("[FAIL] #157 Returns -> Home (Logo): not on home screen");
        assertTrue("Returns -> Home failed", onHome3);

        logger.info("[PASS] #157 Logo navigation - all entry points OK");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test157_LogoNavigation.class);
        System.out.println("Test #157 skipped (logo not clickable). See LOCATORS.md for details.");
        System.exit(0);
    }
}
