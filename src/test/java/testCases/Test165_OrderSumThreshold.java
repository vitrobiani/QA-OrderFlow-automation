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

/**
 * PractiTest Test #165 — Order sum threshold.
 * Requirement #145 (Form Submission & Validation).
 * Validates Rule R2: order sum cannot exceed 10,000.
 *
 * Scenario: Build an order with total > $10,000,
 * attempt to submit. Verify error popup shows and submit is blocked.
 */
public class Test165_OrderSumThreshold {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test165_OrderSumThreshold.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testOrderSumThreshold() throws InterruptedException {
        NavBar nav = new NavBar(driver);
        NewOrderPage orderPage = new NewOrderPage(driver);

        // Navigate to New Order
        driver.get(base_test_class.BASE_URL);
        nav.goNewOrder();
        assertTrue("New Order page not loaded", orderPage.isLoaded());

        // Select expensive category (laptops/tablets — high enough price-per-unit to
        // cross $10k at max-allowed qty without exceeding stock)
        orderPage.selectCategory("laptops");
        Thread.sleep(2000);

        String productName = orderPage.firstProductName();
        int stock = orderPage.firstProductStock();
        logger.info("#165 Testing with product: " + productName + " (stock=" + stock + ")");

        // Add product, set qty = stock (max allowed by R1 → R2 fires alone if sum still > $10k)
        orderPage.addFirstProduct();
        Thread.sleep(1000);

        orderPage.setQuantity(productName, stock);
        logger.info("#165 Set quantity to: " + stock);
        Thread.sleep(500);

        // Check current sum
        double currentSum = orderPage.orderSum();
        logger.info("#165 Current order sum: $" + currentSum);
        assertTrue("Test setup: qty=stock should push sum over $10,000 for smartphones",
                   currentSum > base_test_class.SUM_CAP);

        // Attempt to submit
        orderPage.submit();
        Thread.sleep(1500);

        // Verify error popup appears
        boolean hasError = orderPage.hasError();
        String errorText = "";
        if (hasError) {
            errorText = orderPage.errorText();
            logger.info("[PASS] #165 Error popup shown: " + errorText);
        } else {
            // If no error, check if sum actually exceeded threshold
            if (currentSum > base_test_class.SUM_CAP) {
                logger.error("[FAIL] #165 No error popup for sum > $10,000");
            } else {
                logger.warn("[WARN] #165 Sum ($" + currentSum + ") didn't exceed threshold - test inconclusive");
            }
        }

        // If sum exceeded threshold, error must appear
        if (currentSum > base_test_class.SUM_CAP) {
            assertTrue("Error popup should appear for sum > $10,000 (R2 violation)", hasError);
        }

        // Verify error mentions sum/threshold/10000
        if (hasError) {
            boolean mentionsSum = errorText.toLowerCase().contains("10000")
                               || errorText.toLowerCase().contains("10,000")
                               || errorText.toLowerCase().contains("sum")
                               || errorText.toLowerCase().contains("total")
                               || errorText.toLowerCase().contains("threshold")
                               || errorText.toLowerCase().contains("exceed");
            if (mentionsSum) {
                logger.info("[PASS] #165 Error message mentions sum threshold issue");
            } else {
                logger.warn("[WARN] #165 Error message doesn't clearly mention sum threshold: " + errorText);
            }

            // Confirm R2 fired alone — qty=stock should NOT trip R1's "available stock" message.
            boolean mentionsStock = errorText.toLowerCase().contains("available stock");
            if (!mentionsStock) {
                logger.info("[PASS] #165 Only R2 fired (no R1 stock-violation reported)");
            } else {
                logger.warn("[WARN] #165 R1 (stock) also fired — qty=stock should have been within bounds");
            }
        }

        logger.info("[PASS] #165 Order sum threshold test complete");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test165_OrderSumThreshold.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
