package testCases;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.internal.TextListener;
import org.openqa.selenium.WebDriver;

import pages.NavBar;

/**
 * PractiTest Test #156 — Returns navigation.
 * Requirement #134 (Navigation / UI).
 *
 * Scenario: From Home, New Order, Order History, and via the Home page
 * quick-nav card ("carousel"), navigate to Returns screen.
 */
public class Test156_ReturnsNavigation {

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
    public void testReturnsNavigation() {
        NavBar nav = new NavBar(driver);

        // From Home -> Returns (nav link)
        driver.get(base_test_class.BASE_URL);
        nav.goReturns();
        boolean onReturns1 = nav.isOnReturns();
        if (onReturns1) logger.info("[PASS] #156 Home -> Returns");
        else            logger.error("[FAIL] #156 Home -> Returns: not on returns screen");
        assertTrue("Home -> Returns failed", onReturns1);

        // From New Order -> Returns
        driver.get(base_test_class.BASE_URL + NavBar.ROUTE_ORDER);
        nav.goReturns();
        boolean onReturns2 = nav.isOnReturns();
        if (onReturns2) logger.info("[PASS] #156 New Order -> Returns");
        else            logger.error("[FAIL] #156 New Order -> Returns: not on returns screen");
        assertTrue("New Order -> Returns failed", onReturns2);

        // From Order History -> Returns
        driver.get(base_test_class.BASE_URL + NavBar.ROUTE_HISTORY);
        nav.goReturns();
        boolean onReturns3 = nav.isOnReturns();
        if (onReturns3) logger.info("[PASS] #156 Order History -> Returns");
        else            logger.error("[FAIL] #156 Order History -> Returns: not on returns screen");
        assertTrue("Order History -> Returns failed", onReturns3);

        // From Home quick-nav card -> Returns
        driver.get(base_test_class.BASE_URL);
        nav.cardReturns();
        boolean onReturns4 = nav.isOnReturns();
        if (onReturns4) logger.info("[PASS] #156 Carousel -> Returns");
        else            logger.error("[FAIL] #156 Carousel -> Returns: not on returns screen");
        assertTrue("Carousel -> Returns failed", onReturns4);

        logger.info("[PASS] #156 Returns navigation - all entry points OK");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test156_ReturnsNavigation.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
