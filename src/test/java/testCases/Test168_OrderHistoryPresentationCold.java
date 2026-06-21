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
import pages.OrderHistoryPage;

/**
 * PractiTest Test #168 — Order History presentation (cold state).
 * Requirement #148 (Order history).
 *
 * Scenario: Open Order History with no orders submitted.
 * Verify empty state is displayed correctly.
 */
public class Test168_OrderHistoryPresentationCold {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test168_OrderHistoryPresentationCold.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testOrderHistoryPresentationCold() throws InterruptedException {
        NavBar nav = new NavBar(driver);
        OrderHistoryPage historyPage = new OrderHistoryPage(driver);

        // Navigate directly to Order History (cold state - no orders)
        driver.get(base_test_class.BASE_URL);
        nav.goOrderHistory();
        Thread.sleep(1000);

        // Verify page loaded
        boolean loaded = historyPage.isLoaded();
        if (loaded) {
            logger.info("#168 Order History page loaded");
        } else {
            logger.error("[FAIL] #168 Order History page not loaded");
        }
        assertTrue("Order History page should load", loaded);

        // Verify empty state is shown
        boolean isEmpty = historyPage.isEmpty();
        if (isEmpty) {
            logger.info("[PASS] #168 Empty state displayed correctly");
        } else {
            logger.error("[FAIL] #168 Empty state not shown in cold state");
        }
        assertTrue("Cold state should show empty message", isEmpty);

        // Verify subtitle shows 0 orders
        String subtitle = historyPage.subtitleText();
        logger.info("#168 Subtitle: " + subtitle);

        boolean showsZero = subtitle.contains("0");
        if (showsZero) {
            logger.info("[PASS] #168 Subtitle indicates 0 orders");
        } else {
            logger.warn("[WARN] #168 Subtitle doesn't clearly show 0 orders: " + subtitle);
        }

        // Verify no order rows
        int rowCount = historyPage.rows().size();
        logger.info("#168 Order rows count: " + rowCount);

        boolean noRows = rowCount == 0;
        if (noRows) {
            logger.info("[PASS] #168 No order rows in cold state");
        } else {
            logger.error("[FAIL] #168 Unexpected order rows in cold state: " + rowCount);
        }
        assertTrue("Should have no order rows in cold state", noRows);

        logger.info("[PASS] #168 Order History presentation (cold) test complete");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test168_OrderHistoryPresentationCold.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
