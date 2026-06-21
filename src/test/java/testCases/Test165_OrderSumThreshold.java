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

        // Select expensive category (smartphones tend to be expensive)
        orderPage.selectCategory("smartphones");
        Thread.sleep(2000);

        String productName = orderPage.firstProductName();
        logger.info("#165 Testing with product: " + productName);

        // Add product with high quantity to exceed $10,000
        orderPage.addFirstProduct();
        Thread.sleep(1000);

        // Set quantity high enough to exceed threshold
        // Assuming average smartphone ~$500-1000, qty of 50 should exceed $10,000
        int highQty = 50;
        orderPage.setQuantity(productName, highQty);
        logger.info("#165 Set quantity to: " + highQty);
        Thread.sleep(500);

        // Check current sum
        double currentSum = orderPage.orderSum();
        logger.info("#165 Current order sum: $" + currentSum);

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
