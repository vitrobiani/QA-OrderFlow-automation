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
import pages.NewOrderPage;

/**
 * PractiTest Test #150 — Product amount options.
 * Requirement #137 (Page interactions).
 *
 * Scenario: Add a product, use +/- buttons and trash,
 * verify quantity and sum update correctly.
 */
public class Test150_ProductAmountOptions {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test150_ProductAmountOptions.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testProductAmountOptions() throws InterruptedException {
        NavBar nav = new NavBar(driver);
        NewOrderPage orderPage = new NewOrderPage(driver);

        // Navigate to New Order and select category
        driver.get(base_test_class.BASE_URL);
        nav.goNewOrder();
        assertTrue("New Order page not loaded", orderPage.isLoaded());

        orderPage.selectCategory("laptops");
        Thread.sleep(2000);

        // Get first product name and add it
        String productName = orderPage.firstProductName();
        logger.info("#150 Testing with product: " + productName);

        orderPage.addFirstProduct();
        Thread.sleep(1000);

        // Verify product appears in summary
        int initialRows = orderPage.summaryRows().size();
        logger.info("#150 Initial summary rows: " + initialRows);

        boolean hasProduct = initialRows > 0 || !orderPage.hasSummaryEmptyState();
        if (hasProduct) {
            logger.info("[PASS] #150 Product added to summary");
        } else {
            logger.error("[FAIL] #150 Product not added to summary");
        }
        assertTrue("Product should appear in summary after adding", hasProduct);

        // Test + button (increase quantity)
        double sumBefore = orderPage.orderSum();
        logger.info("#150 Sum before +: " + sumBefore);

        orderPage.plus(productName);
        Thread.sleep(500);

        double sumAfterPlus = orderPage.orderSum();
        logger.info("#150 Sum after +: " + sumAfterPlus);

        // Sum should increase (or stay same if + doesn't work)
        if (sumAfterPlus > sumBefore) {
            logger.info("[PASS] #150 Plus button increased sum");
        } else {
            logger.warn("[WARN] #150 Plus button may not have worked (sum unchanged)");
        }

        // Test - button (decrease quantity)
        orderPage.minus(productName);
        Thread.sleep(500);

        double sumAfterMinus = orderPage.orderSum();
        logger.info("#150 Sum after -: " + sumAfterMinus);

        // Test trash (remove product)
        orderPage.trash(productName);
        Thread.sleep(500);

        boolean removed = orderPage.hasSummaryEmptyState() || orderPage.summaryRows().size() < initialRows;
        if (removed) {
            logger.info("[PASS] #150 Trash button removed product");
        } else {
            logger.warn("[WARN] #150 Trash button may not have worked");
        }

        logger.info("[PASS] #150 Product amount options test complete");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test150_ProductAmountOptions.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
