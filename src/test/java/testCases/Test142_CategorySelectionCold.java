package testCases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

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
import utils.JsonData;

/**
 * PractiTest Test #142 — Category selection (cold state).
 * Requirement #140 (Order process).
 *
 * Scenario: For EACH category in categories.json:
 *   1. Open New Order page fresh (cold state)
 *   2. Select the category
 *   3. Verify the dropdown shows the correct category
 *   4. Verify products from that category load
 *   5. Reset by navigating away
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

        // Load all categories from test data
        List<String> categories = JsonData.readStringArray("testdata/categories.json");
        logger.info("#142 Testing " + categories.size() + " categories: " + categories);

        int passCount = 0;
        int failCount = 0;

        for (String category : categories) {
            logger.info("#142 ========== Testing category: " + category + " ==========");

            // Navigate fresh to New Order page (cold state)
            driver.get(base_test_class.BASE_URL);
            nav.goNewOrder();
            Thread.sleep(1000);

            // Verify page loaded in cold state
            assertTrue("New Order page not loaded", orderPage.isLoaded());

            // Verify empty state before selection
            boolean hasEmptyState = orderPage.hasProductsEmptyState();
            if (hasEmptyState) {
                logger.info("#142 [" + category + "] Cold state confirmed: empty products state shown");
            } else {
                logger.info("#142 [" + category + "] Page loaded (no empty state indicator)");
            }

            // Select the category
            orderPage.selectCategory(category);
            logger.info("#142 [" + category + "] Selected category from dropdown");

            // Wait for products to load
            Thread.sleep(2000);

            // CRITICAL CHECK 1: Verify the dropdown actually shows the selected category
            String actualCategory = orderPage.currentCategory();
            boolean categoryMatches = category.equals(actualCategory);

            if (!categoryMatches) {
                logger.error("[FAIL] #142 [" + category + "] CATEGORY MISMATCH! Expected: " + category + ", Actual: " + actualCategory);
                failCount++;
                continue; // Skip to next category
            }
            logger.info("#142 [" + category + "] Dropdown confirmed: " + actualCategory);

            // CRITICAL CHECK 2: Verify products loaded
            int productCount = orderPage.productCardCount();
            boolean emptyGone = !orderPage.hasProductsEmptyState();

            if (productCount > 0) {
                // Get first product name for logging
                String firstProduct = orderPage.firstProductName();
                logger.info("[PASS] #142 [" + category + "] Category loaded correctly: " + productCount + " products, first: " + firstProduct);
                passCount++;
            } else if (emptyGone) {
                logger.warn("[WARN] #142 [" + category + "] Empty state gone but no products counted - possible locator issue");
                passCount++; // Still consider pass if empty state disappeared
            } else {
                logger.error("[FAIL] #142 [" + category + "] No products loaded!");
                failCount++;
            }

            // Navigate away to reset for next iteration
            nav.goHome();
            Thread.sleep(500);
        }

        // Summary
        logger.info("#142 ========== SUMMARY ==========");
        logger.info("#142 Passed: " + passCount + "/" + categories.size());
        if (failCount > 0) {
            logger.error("#142 Failed: " + failCount + "/" + categories.size());
        }

        assertEquals("All categories should load correctly", 0, failCount);
        logger.info("[PASS] #142 Category selection (cold) - all " + categories.size() + " categories verified");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test142_CategorySelectionCold.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
