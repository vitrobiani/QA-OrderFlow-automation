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
 * PractiTest Test #146 — Category selection (hot state).
 * Requirement #140 (Order process).
 *
 * Scenario: Start with first category, then switch through ALL categories:
 *   1. Select first category, verify products load
 *   2. For each subsequent category:
 *      - Switch to it (hot state - another category already selected)
 *      - VERIFY dropdown actually shows the new category
 *      - VERIFY products actually changed (different from previous category)
 *
 * This test will catch bugs like "stuck in furniture category".
 */
public class Test146_CategorySelectionHot {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test146_CategorySelectionHot.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testCategorySelectionHot() throws InterruptedException {
        NavBar nav = new NavBar(driver);
        NewOrderPage orderPage = new NewOrderPage(driver);

        // Load all categories from test data
        List<String> categories = JsonData.readStringArray("testdata/categories.json");
        logger.info("#146 Testing hot switching through " + categories.size() + " categories: " + categories);

        // Navigate to New Order
        driver.get(base_test_class.BASE_URL);
        nav.goNewOrder();
        assertTrue("New Order page not loaded", orderPage.isLoaded());

        int switchCount = 0;
        int passCount = 0;
        int failCount = 0;

        String previousCategory = null;
        String previousFirstProduct = null;

        for (int i = 0; i < categories.size(); i++) {
            String targetCategory = categories.get(i);

            if (i == 0) {
                // First category - cold selection
                logger.info("#146 ========== Initial category: " + targetCategory + " ==========");
                orderPage.selectCategory(targetCategory);
                Thread.sleep(2000);

                // Verify dropdown shows correct category
                String actualCategory = orderPage.currentCategory();
                if (!targetCategory.equals(actualCategory)) {
                    logger.error("[FAIL] #146 [" + targetCategory + "] Initial selection failed! Dropdown shows: " + actualCategory);
                    failCount++;
                    continue;
                }

                previousFirstProduct = orderPage.firstProductName();
                previousCategory = targetCategory;

                int productCount = orderPage.productCardCount();
                logger.info("#146 [" + targetCategory + "] Initial: " + productCount + " products, first: " + previousFirstProduct);

                if (productCount > 0) {
                    logger.info("[PASS] #146 [" + targetCategory + "] Initial category loaded correctly");
                    passCount++;
                } else {
                    logger.error("[FAIL] #146 [" + targetCategory + "] Initial category failed - no products");
                    failCount++;
                }
            } else {
                // Hot switch - another category already selected
                logger.info("#146 ========== Switching: " + previousCategory + " -> " + targetCategory + " ==========");

                orderPage.selectCategory(targetCategory);
                switchCount++;
                Thread.sleep(2000);

                // CRITICAL CHECK 1: Verify dropdown actually changed to new category
                String actualCategory = orderPage.currentCategory();
                boolean dropdownChanged = targetCategory.equals(actualCategory);

                if (!dropdownChanged) {
                    logger.error("[FAIL] #146 [" + previousCategory + " -> " + targetCategory + "] STUCK! Dropdown still shows: " + actualCategory);
                    logger.error("       This indicates a bug - cannot switch away from " + previousCategory);
                    failCount++;
                    // Don't update previous - we're stuck
                    continue;
                }
                logger.info("#146 [" + targetCategory + "] Dropdown confirmed: " + actualCategory);

                // CRITICAL CHECK 2: Verify products actually changed
                String currentFirstProduct = orderPage.firstProductName();
                int productCount = orderPage.productCardCount();
                boolean productsChanged = !currentFirstProduct.equals(previousFirstProduct);

                logger.info("#146 [" + targetCategory + "] Products: " + productCount + ", first: " + currentFirstProduct);

                if (!productsChanged) {
                    logger.error("[FAIL] #146 [" + previousCategory + " -> " + targetCategory + "] PRODUCTS DID NOT CHANGE!");
                    logger.error("       Previous first product: " + previousFirstProduct);
                    logger.error("       Current first product:  " + currentFirstProduct);
                    logger.error("       This indicates the category switch didn't actually work!");
                    failCount++;
                } else if (productCount > 0) {
                    logger.info("[PASS] #146 [" + previousCategory + " -> " + targetCategory + "] Switch successful!");
                    logger.info("       Products changed from '" + previousFirstProduct + "' to '" + currentFirstProduct + "'");
                    passCount++;
                } else {
                    logger.error("[FAIL] #146 [" + previousCategory + " -> " + targetCategory + "] No products after switch!");
                    failCount++;
                }

                // Update previous for next iteration
                previousFirstProduct = currentFirstProduct;
                previousCategory = targetCategory;
            }
        }

        // Summary
        logger.info("#146 ========== SUMMARY ==========");
        logger.info("#146 Total switches attempted: " + switchCount);
        logger.info("#146 Passed: " + passCount + "/" + categories.size());
        if (failCount > 0) {
            logger.error("#146 Failed: " + failCount + "/" + categories.size());
            logger.error("#146 NOTE: If 'STUCK' errors appeared, there's a bug preventing category switching!");
        }

        assertEquals("All category switches should work correctly", 0, failCount);
        logger.info("[PASS] #146 Category selection (hot) - all " + switchCount + " switches verified");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test146_CategorySelectionHot.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
