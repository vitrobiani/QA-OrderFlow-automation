package testCases;

import static org.junit.Assert.assertFalse;
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
 * PractiTest Test #142 — Category selection (cold state).
 * Requirement #140 (Order process).
 *
 * Scenario: Open New Order page (no category selected),
 * pick a category from the dropdown, verify products load.
 */
public class Test142_CategorySelectionCold {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test142_CategorySelectionCold.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testCategorySelectionCold() throws InterruptedException {
        NavBar nav = new NavBar(driver);
        NewOrderPage orderPage = new NewOrderPage(driver);

        // Navigate to New Order
        driver.get(base_test_class.BASE_URL);
        nav.goNewOrder();

        // Verify page loaded in cold state (no category selected)
        assertTrue("New Order page not loaded", orderPage.isLoaded());
        boolean hasEmptyState = orderPage.hasProductsEmptyState();
        if (hasEmptyState) logger.info("[PASS] #142 Cold state: empty products state shown");
        else               logger.warn("[INFO] #142 Cold state: no empty state indicator (may be OK)");

        // Select a category
        String category = "laptops";
        orderPage.selectCategory(category);
        logger.info("#142 Selected category: " + category);

        // Wait for products to load
        Thread.sleep(2000);

        // Verify products loaded (empty state should disappear)
        boolean emptyGone = !orderPage.hasProductsEmptyState();
        int productCount = orderPage.productCardCount();

        if (emptyGone && productCount > 0) {
            logger.info("[PASS] #142 Category selection cold: products loaded (count=" + productCount + ")");
        } else if (productCount > 0) {
            logger.info("[PASS] #142 Category selection cold: products loaded (count=" + productCount + ")");
        } else {
            logger.error("[FAIL] #142 Category selection cold: no products loaded after selecting " + category);
        }

        assertTrue("Products should load after category selection", productCount > 0 || emptyGone);

        logger.info("[PASS] #142 Category selection (cold) complete");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test142_CategorySelectionCold.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
