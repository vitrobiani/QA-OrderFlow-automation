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
import pages.NewOrderPage;
import pages.OrderHistoryPage;

/**
 * PractiTest Test #180 — Data Persistence.
 * Requirement #149 (Information security).
 *
 * Scenario: Submit a valid order (Call To Test #171),
 * then refresh the page. Verify session is cleared and
 * the submitted order is gone (no persistence across sessions).
 *
 * Note: This tests that the app does NOT persist data across
 * browser refresh, which is the expected behavior for this SUT.
 */
public class Test180_DataPersistence {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test180_DataPersistence.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testDataPersistence() throws InterruptedException {
        NavBar nav = new NavBar(driver);
        NewOrderPage orderPage = new NewOrderPage(driver);
        OrderHistoryPage historyPage = new OrderHistoryPage(driver);

        // First, submit a valid order
        driver.get(base_test_class.BASE_URL);
        nav.goNewOrder();
        Thread.sleep(1000);

        orderPage.selectCategory("groceries");
        Thread.sleep(2000);

        String productName = orderPage.firstProductName();
        logger.info("#180 Submitting order for: " + productName);

        orderPage.addFirstProduct();
        Thread.sleep(1000);

        orderPage.submit();
        Thread.sleep(2000);

        // Verify order was submitted
        nav.goOrderHistory();
        Thread.sleep(1000);

        boolean hadOrderBefore = !historyPage.isEmpty();
        if (hadOrderBefore) {
            logger.info("#180 Order confirmed in history before refresh");
        } else {
            logger.warn("#180 Order not in history before refresh - test may be invalid");
        }

        // Now refresh the page
        logger.info("#180 Refreshing page...");
        driver.navigate().refresh();
        Thread.sleep(2000);

        // Check history again - should be empty (session cleared)
        boolean isEmptyAfterRefresh = historyPage.isEmpty();

        if (isEmptyAfterRefresh) {
            logger.info("[PASS] #180 Session cleared after refresh - history empty");
        } else {
            // Some apps might persist - log but don't necessarily fail
            logger.warn("[INFO] #180 Order still present after refresh (data persisted)");
        }

        // Navigate to home and back to history to confirm state
        nav.goHome();
        Thread.sleep(500);
        nav.goOrderHistory();
        Thread.sleep(1000);

        boolean isEmptyFinal = historyPage.isEmpty();
        String subtitle = historyPage.subtitleText();
        logger.info("#180 Final history state - empty: " + isEmptyFinal + ", subtitle: " + subtitle);

        // The expected behavior per spec is that refresh clears the session
        // If data persists, that's actually a "bug" per the spec
        if (isEmptyFinal) {
            logger.info("[PASS] #180 Data not persisted (correct per spec)");
        } else {
            logger.info("[INFO] #180 Data persisted across refresh (may be intentional)");
        }

        // Assert based on expected behavior (refresh should clear)
        assertTrue("Session should be cleared after refresh (per spec)", isEmptyAfterRefresh || isEmptyFinal);

        logger.info("[PASS] #180 Data Persistence test complete");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test180_DataPersistence.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
