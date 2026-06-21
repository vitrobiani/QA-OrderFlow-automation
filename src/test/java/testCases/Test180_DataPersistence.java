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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import pages.NavBar;
import pages.NewOrderPage;
import pages.OrderHistoryPage;

/**
 * PractiTest Test #180 — Data Persistence.
 * Requirement #149 (Information security).
 *
 * Scenario: Submit a valid order (same as Test #171/174),
 * then refresh the page. Verify export button is gone
 * (same check as Test #170 cold state).
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

        // === PART 1: Place a good order (same as Test174/Test171) ===
        driver.get(base_test_class.BASE_URL);
        nav.goNewOrder();
        assertTrue("New Order page not loaded", orderPage.isLoaded());

        orderPage.selectCategory("laptops");
        Thread.sleep(2000);

        String productName = orderPage.firstProductName();
        logger.info("#180 Ordering product: " + productName);

        orderPage.addFirstProduct();
        Thread.sleep(1000);

        logger.info("#180 Submitting order...");
        orderPage.submitAndConfirm();
        Thread.sleep(2000);

        if (orderPage.hasConfirmDialog()) {
            orderPage.confirm();
            Thread.sleep(2000);
        }

        // Navigate to Order History to confirm order exists
        nav.goOrderHistory();
        Thread.sleep(1500);

        // Verify export button exists after placing order
        By exportBtn = By.id("btn-export-csv");
        boolean hasExportBefore = driver.findElements(exportBtn).size() > 0;
        logger.info("#180 Export button before refresh: " + hasExportBefore);

        // === PART 2: Refresh the page ===
        logger.info("#180 Refreshing page...");
        driver.navigate().refresh();
        Thread.sleep(2000);

        // === PART 3: Run Test170 logic (check NO export button in cold state) ===
        boolean hasExportAfter = driver.findElements(exportBtn).size() > 0;

        if (!hasExportAfter) {
            logger.info("[PASS] #180 No export button after refresh (data cleared - cold state)");
        } else {
            logger.error("[FAIL] #180 Export button still present after refresh (data persisted)");
        }

        assertFalse("Export button should NOT be present after refresh (cold state)", hasExportAfter);

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
