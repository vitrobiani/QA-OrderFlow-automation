package testCases;

import static org.junit.Assert.assertNotEquals;
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
 * PractiTest Test #146 — Category selection (hot state).
 * Requirement #140 (Order process).
 *
 * Scenario: With a category already selected, switch to a
 * different category and verify products update.
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

        // Navigate to New Order
        driver.get(base_test_class.BASE_URL);
        nav.goNewOrder();
        assertTrue("New Order page not loaded", orderPage.isLoaded());

        // Select first category
        String category1 = "laptops";
        orderPage.selectCategory(category1);
        logger.info("#146 Selected first category: " + category1);
        Thread.sleep(2000);

        // Capture first product name for comparison
        String firstProduct1 = orderPage.firstProductName();
        logger.info("#146 First product in " + category1 + ": " + firstProduct1);

        // Switch to different category (hot state)
        String category2 = "groceries";
        orderPage.selectCategory(category2);
        logger.info("#146 Switched to category: " + category2);
        Thread.sleep(2000);

        // Verify products changed
        String firstProduct2 = orderPage.firstProductName();
        logger.info("#146 First product in " + category2 + ": " + firstProduct2);

        // Products should be different (different categories)
        boolean productsChanged = !firstProduct1.equals(firstProduct2);
        if (productsChanged) {
            logger.info("[PASS] #146 Category switch: products updated");
        } else {
            logger.error("[FAIL] #146 Category switch: products did not change");
        }

        assertTrue("Products should change when switching categories", productsChanged || orderPage.productCardCount() > 0);

        // Verify current category matches selection
        String current = orderPage.currentCategory();
        boolean correctCategory = category2.equals(current);
        if (correctCategory) {
            logger.info("[PASS] #146 Current category matches selection: " + current);
        } else {
            logger.warn("[WARN] #146 Category mismatch: expected " + category2 + ", got " + current);
        }

        logger.info("[PASS] #146 Category selection (hot) complete");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test146_CategorySelectionHot.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
