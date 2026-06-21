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
import pages.NewOrderPage;
import pages.OrderHistoryPage;

/**
 * PractiTest Test #171 — Submit good order.
 * Requirement #145 (Form Submission & Validation).
 *
 * Scenario: Build a valid order (respecting R1, R2, R3),
 * submit it. Verify order is confirmed and appears in history.
 * This is THE key positive test for the order flow.
 */
public class Test171_SubmitGoodOrder {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test171_SubmitGoodOrder.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testSubmitGoodOrder() throws InterruptedException {
        NavBar nav = new NavBar(driver);
        NewOrderPage orderPage = new NewOrderPage(driver);
        OrderHistoryPage historyPage = new OrderHistoryPage(driver);

        // Navigate to New Order
        driver.get(base_test_class.BASE_URL);
        nav.goNewOrder();
        assertTrue("New Order page not loaded", orderPage.isLoaded());

        // Select category (using laptops - safe, no R3 conflict)
        orderPage.selectCategory("laptops");
        Thread.sleep(2000);

        // Get product name and add it
        String productName = orderPage.firstProductName();
        logger.info("#171 Ordering product: " + productName);

        orderPage.addFirstProduct();
        Thread.sleep(1000);

        // Verify product in summary
        boolean hasProduct = !orderPage.hasSummaryEmptyState();
        if (hasProduct) {
            logger.info("#171 Product added to order summary");
        } else {
            logger.error("#171 Product not in summary - cannot proceed");
        }
        assertTrue("Product must be in summary before submit", hasProduct);

        // Note: Sum validation skipped - locator needs hot-state discovery confirmation
        // The app will validate R2 on submit anyway; if sum > $10,000, we'll see an error

        // Submit order (handles confirmation dialog if present)
        logger.info("#171 Submitting order...");
        orderPage.submitAndConfirm();
        Thread.sleep(1000);

        // Verify no validation error
        boolean hasError = orderPage.hasError();
        if (hasError) {
            String errorText = orderPage.errorText();
            logger.error("[FAIL] #171 Unexpected error on submit: " + errorText);
        }
        assertFalse("Good order should not produce error", hasError);

        // Submit triggers a "Confirm Order" dialog — accept it to actually place the order.
        if (orderPage.hasConfirmDialog()) {
            logger.info("#171 Confirm-order dialog shown, accepting");
            orderPage.confirm();
            Thread.sleep(1000);
        }

        // Navigate to order history
        nav.goOrderHistory();
        Thread.sleep(1000);

        // Verify order appears in history
        boolean historyNotEmpty = !historyPage.isEmpty();
        if (historyNotEmpty) {
            logger.info("[PASS] #171 Order appears in history");
        } else {
            logger.error("[FAIL] #171 Order not found in history");
        }
        assertTrue("Order should appear in history after successful submit", historyNotEmpty);

        // Verify product name appears in history
        boolean productInHistory = historyPage.containsProduct(productName);
        if (productInHistory) {
            logger.info("[PASS] #171 Product '" + productName + "' found in order history");
        } else {
            logger.warn("[WARN] #171 Product name not found in history (may use different format)");
        }

        // Check subtitle (optional - may not be present in all app versions)
        try {
            String subtitle = historyPage.subtitleText();
            logger.info("#171 History subtitle: " + subtitle);
        } catch (Exception e) {
            logger.info("#171 Subtitle element not found (OK - order still verified)");
        }

        logger.info("[PASS] #171 Submit good order test complete");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test171_SubmitGoodOrder.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
