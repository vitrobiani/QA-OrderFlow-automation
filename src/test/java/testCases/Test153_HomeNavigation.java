package testCases;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.TextListener;
import org.openqa.selenium.WebDriver;

import pages.NavBar;

/**
 * PractiTest Test #153 — Home navigation.
 * Requirement #134 (Navigation / UI).
 *
 * Scenario: From each non-home page (New Order, Order History, Returns),
 * click the "Home" nav link and verify we land on the Home screen.
 */
public class Test153_HomeNavigation {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test153_HomeNavigation.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testHomeNavigation() {
        NavBar nav = new NavBar(driver);

        // From New Order -> Home
        driver.get(base_test_class.BASE_URL + NavBar.ROUTE_ORDER);
        nav.goHome();
        boolean onHome1 = nav.isOnHome();
        if (onHome1) logger.info("[PASS] #153 New Order -> Home");
        else         logger.error("[FAIL] #153 New Order -> Home: not on home screen");
        assertTrue("New Order -> Home failed", onHome1);

        // From Order History -> Home
        driver.get(base_test_class.BASE_URL + NavBar.ROUTE_HISTORY);
        nav.goHome();
        boolean onHome2 = nav.isOnHome();
        if (onHome2) logger.info("[PASS] #153 Order History -> Home");
        else         logger.error("[FAIL] #153 Order History -> Home: not on home screen");
        assertTrue("Order History -> Home failed", onHome2);

        // From Returns -> Home
        driver.get(base_test_class.BASE_URL + NavBar.ROUTE_RETURNS);
        nav.goHome();
        boolean onHome3 = nav.isOnHome();
        if (onHome3) logger.info("[PASS] #153 Returns -> Home");
        else         logger.error("[FAIL] #153 Returns -> Home: not on home screen");
        assertTrue("Returns -> Home failed", onHome3);

        logger.info("[PASS] #153 Home navigation - all entry points OK");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test153_HomeNavigation.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
