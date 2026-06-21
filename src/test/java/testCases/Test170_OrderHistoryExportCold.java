package testCases;

import static org.junit.Assert.assertFalse;
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
 * PractiTest Test #170 — Order History export (cold state).
 * Requirement #148 (Order history).
 *
 * Scenario: Open Order History with no orders submitted.
 * Verify NO export button is present (nothing to export).
 */
public class Test170_OrderHistoryExportCold {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test170_OrderHistoryExportCold.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testOrderHistoryExportCold() throws InterruptedException {
        NavBar nav = new NavBar(driver);
        OrderHistoryPage historyPage = new OrderHistoryPage(driver);

        // Navigate directly to Order History (cold state - no orders)
        driver.get(base_test_class.BASE_URL);
        nav.goOrderHistory();
        Thread.sleep(1000);

        // Verify page loaded
        assertTrue("Order History page should load", historyPage.isLoaded());

        // Verify empty state (confirms cold state)
        boolean isEmpty = historyPage.isEmpty();
        if (isEmpty) {
            logger.info("#170 Confirmed cold state (empty history)");
        } else {
            logger.warn("#170 History not empty - test may not be valid");
        }

        // Check for export button - should NOT be present in cold state
        boolean hasExport = historyPage.hasExportButton();

        if (!hasExport) {
            logger.info("[PASS] #170 No export button in cold state (correct behavior)");
        } else {
            logger.error("[FAIL] #170 Export button present in cold state (should be hidden)");
        }

        assertFalse("Export button should NOT be present when history is empty", hasExport);

        logger.info("[PASS] #170 Order History export (cold) test complete");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test170_OrderHistoryExportCold.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
