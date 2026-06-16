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
 * PractiTest Test #154 — New Order navigation.
 * Requirement #134 (Navigation / UI).
 *
 * Scenario: From Home, Order History, Returns, and via the Home page
 * quick-nav card ("carousel"), navigate to New Order screen.
 */
public class Test154_NewOrderNavigation {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test154_NewOrderNavigation.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testNewOrderNavigation() {
        NavBar nav = new NavBar(driver);

        // From Home -> New Order (nav link)
        driver.get(base_test_class.BASE_URL);
        nav.goNewOrder();
        boolean onNewOrder1 = nav.isOnNewOrder();
        if (onNewOrder1) logger.info("[PASS] #154 Home -> New Order");
        else             logger.error("[FAIL] #154 Home -> New Order: not on new-order screen");
        assertTrue("Home -> New Order failed", onNewOrder1);

        // From Order History -> New Order
        driver.get(base_test_class.BASE_URL + NavBar.ROUTE_HISTORY);
        nav.goNewOrder();
        boolean onNewOrder2 = nav.isOnNewOrder();
        if (onNewOrder2) logger.info("[PASS] #154 Order History -> New Order");
        else             logger.error("[FAIL] #154 Order History -> New Order: not on new-order screen");
        assertTrue("Order History -> New Order failed", onNewOrder2);

        // From Returns -> New Order
        driver.get(base_test_class.BASE_URL + NavBar.ROUTE_RETURNS);
        nav.goNewOrder();
        boolean onNewOrder3 = nav.isOnNewOrder();
        if (onNewOrder3) logger.info("[PASS] #154 Returns -> New Order");
        else             logger.error("[FAIL] #154 Returns -> New Order: not on new-order screen");
        assertTrue("Returns -> New Order failed", onNewOrder3);

        // From Home quick-nav card -> New Order
        driver.get(base_test_class.BASE_URL);
        nav.cardNewOrder();
        boolean onNewOrder4 = nav.isOnNewOrder();
        if (onNewOrder4) logger.info("[PASS] #154 Carousel -> New Order");
        else             logger.error("[FAIL] #154 Carousel -> New Order: not on new-order screen");
        assertTrue("Carousel -> New Order failed", onNewOrder4);

        logger.info("[PASS] #154 New Order navigation - all entry points OK");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test154_NewOrderNavigation.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
