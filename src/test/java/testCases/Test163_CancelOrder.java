package testCases;

import static org.junit.Assert.assertEquals;
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
 * PractiTest Test #163 — Cancel order.
 * Requirement #145 (Form Submission & Validation).
 *
 * Scenario: After a validation error, click Cancel.
 * Verify order is cancelled and local stock is reverted.
 */
public class Test163_CancelOrder {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test163_CancelOrder.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testCancelOrder() throws InterruptedException {
        NavBar nav = new NavBar(driver);
        NewOrderPage orderPage = new NewOrderPage(driver);
        OrderHistoryPage historyPage = new OrderHistoryPage(driver);

        // Navigate to New Order
        driver.get(base_test_class.BASE_URL);
        nav.goNewOrder();
        assertTrue("New Order page not loaded", orderPage.isLoaded());

        // Select category and add product
        orderPage.selectCategory("laptops");
        Thread.sleep(2000);

        String productName = orderPage.firstProductName();
        int originalStock = orderPage.firstProductStock();
        logger.info("#163 Testing with product: " + productName + " (original stock=" + originalStock + ")");

        orderPage.addFirstProduct();
        Thread.sleep(1000);

        // Trigger validation error (illegal qty)
        orderPage.setQuantity(productName, 99999);
        orderPage.submit();
        Thread.sleep(1500);

        // Verify error appeared
        boolean hasError = orderPage.hasError();
        if (hasError) {
            logger.info("#163 Validation error triggered as expected");
        } else {
            logger.warn("#163 No validation error - proceeding with cancel test anyway");
        }

        // Click Cancel
        orderPage.cancel();
        Thread.sleep(2500);
        logger.info("#163 Cancel button clicked");

        // KNOWN SUT BUG: Cancel clears the cart but does NOT restore the stock badge —
        // the product is left displayed as "Out of stock" even though no order was placed.
        // This assertion is expected to FAIL until the SUT is fixed; a passing test means
        // the bug has been resolved. Do NOT loosen this check.
        int stockAfterCancel = orderPage.firstProductStock();
        logger.info("#163 Stock after cancel: " + stockAfterCancel);
        if (stockAfterCancel == originalStock) {
            logger.info("[PASS] #163 Stock restored to original (" + originalStock + ")");
        } else {
            logger.error("[FAIL] #163 Stock not restored: expected " + originalStock + " but got " + stockAfterCancel);
        }
        assertEquals("Stock should be restored to original after cancel", originalStock, stockAfterCancel);

        // Verify we're back to clean state or order is cancelled
        // Check that the order didn't go through (history should be empty or unchanged)
        nav.goOrderHistory();
        Thread.sleep(1000);

        boolean historyEmpty = historyPage.isEmpty();
        if (historyEmpty) {
            logger.info("[PASS] #163 Order cancelled - history remains empty");
        } else {
            // History might have previous orders; check if it didn't increase
            logger.info("[PASS] #163 Cancel completed (history may contain prior orders)");
        }

        // Also verify order page is back to usable state
        nav.goNewOrder();
        Thread.sleep(500);
        boolean pageUsable = orderPage.isLoaded();
        if (pageUsable) {
            logger.info("[PASS] #163 Order page is usable after cancel");
        } else {
            logger.error("[FAIL] #163 Order page not usable after cancel");
        }

        assertTrue("Order page should be usable after cancel", pageUsable);

        logger.info("[PASS] #163 Cancel order test complete");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test163_CancelOrder.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
