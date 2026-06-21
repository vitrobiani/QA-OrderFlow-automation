package testCases;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.internal.TextListener;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import pages.NavBar;
import pages.NewOrderPage;

/**
 * PractiTest Test #174 — Order History export button (hot state).
 * Requirement #148 (Order history).
 *
 * Scenario: Submit a valid order (same as Test #171),
 * navigate to Order History, verify export button exists.
 */
public class Test174_OrderHistoryExportHot {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test174_OrderHistoryExportHot.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testOrderHistoryExportHot() throws InterruptedException {
        NavBar nav = new NavBar(driver);
        NewOrderPage orderPage = new NewOrderPage(driver);

        // Submit a valid order (same flow as Test171)
        driver.get(base_test_class.BASE_URL);
        nav.goNewOrder();
        assertTrue("New Order page not loaded", orderPage.isLoaded());

        orderPage.selectCategory("laptops");
        Thread.sleep(2000);

        String productName = orderPage.firstProductName();
        logger.info("#174 Ordering product: " + productName);

        orderPage.addFirstProduct();
        Thread.sleep(1000);

        logger.info("#174 Submitting order...");
        orderPage.submitAndConfirm();
        Thread.sleep(2000);

        if (orderPage.hasConfirmDialog()) {
            orderPage.confirm();
            Thread.sleep(2000);
        }

        // Navigate to Order History
        nav.goOrderHistory();
        Thread.sleep(1500);

        // Check if export button exists (id: btn-export-csv)
        By exportBtn = By.id("btn-export-csv");
        boolean hasExportButton = driver.findElements(exportBtn).size() > 0;

        if (hasExportButton) {
            logger.info("[PASS] #174 Export button (btn-export-csv) found in Order History");
        } else {
            logger.error("[FAIL] #174 Export button (btn-export-csv) NOT found in Order History");
        }
        assertTrue("Export button should be present in Order History after submitting an order", hasExportButton);

        logger.info("[PASS] #174 Order History export button test complete");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test174_OrderHistoryExportHot.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
