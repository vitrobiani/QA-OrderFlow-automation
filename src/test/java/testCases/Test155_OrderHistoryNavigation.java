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
 * PractiTest Test #155 — Order History navigation.
 * Requirement #134 (Navigation / UI).
 *
 * Scenario: From Home, New Order, Returns, and via the Home page
 * quick-nav card ("carousel"), navigate to Order History screen.
 */
public class Test155_OrderHistoryNavigation {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test155_OrderHistoryNavigation.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testOrderHistoryNavigation() {
        NavBar nav = new NavBar(driver);

        // From Home -> Order History (nav link)
        driver.get(base_test_class.BASE_URL);
        nav.goOrderHistory();
        boolean onHistory1 = nav.isOnHistory();
        if (onHistory1) logger.info("[PASS] #155 Home -> Order History");
        else            logger.error("[FAIL] #155 Home -> Order History: not on history screen");
        assertTrue("Home -> Order History failed", onHistory1);

        // From New Order -> Order History
        driver.get(base_test_class.BASE_URL + NavBar.ROUTE_ORDER);
        nav.goOrderHistory();
        boolean onHistory2 = nav.isOnHistory();
        if (onHistory2) logger.info("[PASS] #155 New Order -> Order History");
        else            logger.error("[FAIL] #155 New Order -> Order History: not on history screen");
        assertTrue("New Order -> Order History failed", onHistory2);

        // From Returns -> Order History
        driver.get(base_test_class.BASE_URL + NavBar.ROUTE_RETURNS);
        nav.goOrderHistory();
        boolean onHistory3 = nav.isOnHistory();
        if (onHistory3) logger.info("[PASS] #155 Returns -> Order History");
        else            logger.error("[FAIL] #155 Returns -> Order History: not on history screen");
        assertTrue("Returns -> Order History failed", onHistory3);

        // From Home quick-nav card -> Order History
        driver.get(base_test_class.BASE_URL);
        nav.cardHistory();
        boolean onHistory4 = nav.isOnHistory();
        if (onHistory4) logger.info("[PASS] #155 Carousel -> Order History");
        else            logger.error("[FAIL] #155 Carousel -> Order History: not on history screen");
        assertTrue("Carousel -> Order History failed", onHistory4);

        logger.info("[PASS] #155 Order History navigation - all entry points OK");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test155_OrderHistoryNavigation.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
