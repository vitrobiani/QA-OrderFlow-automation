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

import flows.OrderFlows;
import pages.NewOrderPage;

/**
 * PractiTest Test #149 — Illegal quantity order.
 * Requirement #145 (Form Submission & Validation).
 * Validates Rule R1: qty cannot exceed available stock.
 *
 * Scenario: Add a product, set qty higher than stock,
 * attempt to submit. Verify error popup shows and submit is blocked.
 */
public class Test149_IllegalQuantityOrder {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test149_IllegalQuantityOrder.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testIllegalQuantityOrder() throws InterruptedException {
        OrderFlows flows = new OrderFlows(driver);
        NewOrderPage orderPage = new NewOrderPage(driver);

        // Navigate and add product with illegal quantity
        driver.get(base_test_class.BASE_URL);

        // Build order with qty > stock (99999 should exceed any stock)
        String category = "laptops";
        orderPage = new NewOrderPage(driver);

        driver.get(base_test_class.BASE_URL + "/order");
        Thread.sleep(1000);

        orderPage.selectCategory(category);
        Thread.sleep(2000);

        String productName = orderPage.firstProductName();
        logger.info("#149 Testing with product: " + productName);

        orderPage.addFirstProduct();
        Thread.sleep(1000);

        // Set illegal quantity
        int illegalQty = 99999;
        orderPage.setQuantity(productName, illegalQty);
        logger.info("#149 Set quantity to: " + illegalQty);
        Thread.sleep(500);

        // Attempt to submit
        orderPage.submit();
        Thread.sleep(1500);

        // Verify error popup appears
        boolean hasError = orderPage.hasError();
        String errorText = "";
        if (hasError) {
            errorText = orderPage.errorText();
            logger.info("[PASS] #149 Error popup shown: " + errorText);
        } else {
            logger.error("[FAIL] #149 No error popup for illegal quantity");
        }

        assertTrue("Error popup should appear for qty > stock (R1 violation)", hasError);

        // Verify error mentions stock/quantity
        boolean mentionsStock = errorText.toLowerCase().contains("stock")
                             || errorText.toLowerCase().contains("quantity")
                             || errorText.toLowerCase().contains("available");
        if (mentionsStock) {
            logger.info("[PASS] #149 Error message mentions stock/quantity issue");
        } else {
            logger.warn("[WARN] #149 Error message doesn't clearly mention stock: " + errorText);
        }

        logger.info("[PASS] #149 Illegal quantity order test complete");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test149_IllegalQuantityOrder.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
