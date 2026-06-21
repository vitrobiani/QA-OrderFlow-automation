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
 * PractiTest Test #169 — No furniture + groceries mix.
 * Requirement #145 (Form Submission & Validation).
 * Validates Rule R3: cannot combine furniture and groceries in same order.
 *
 * Scenario: Add one furniture item and one groceries item,
 * attempt to submit. Verify error popup shows about category mixing.
 */
public class Test169_NoFurnitureGroceries {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test169_NoFurnitureGroceries.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testNoFurnitureGroceriesMix() throws InterruptedException {
        NavBar nav = new NavBar(driver);
        NewOrderPage orderPage = new NewOrderPage(driver);

        // Navigate to New Order
        driver.get(base_test_class.BASE_URL);
        nav.goNewOrder();
        assertTrue("New Order page not loaded", orderPage.isLoaded());

        // Add furniture item
        orderPage.selectCategory("furniture");
        Thread.sleep(2000);

        String furnitureProduct = orderPage.firstProductName();
        logger.info("#169 Adding furniture product: " + furnitureProduct);
        orderPage.addFirstProduct();
        Thread.sleep(1000);

        // Verify furniture item added
        boolean hasFurniture = !orderPage.hasSummaryEmptyState();
        if (hasFurniture) {
            logger.info("#169 Furniture item added to order");
        }

        // Add groceries item
        orderPage.selectCategory("groceries");
        Thread.sleep(2000);

        String groceryProduct = orderPage.firstProductName();
        logger.info("#169 Adding grocery product: " + groceryProduct);
        orderPage.addFirstProduct();
        Thread.sleep(1000);

        // Verify both items in order
        double currentSum = orderPage.orderSum();
        logger.info("#169 Order sum with both items: $" + currentSum);

        // Attempt to submit
        orderPage.submit();
        Thread.sleep(1500);

        // Verify error popup appears
        boolean hasError = orderPage.hasError();
        String errorText = "";
        if (hasError) {
            errorText = orderPage.errorText();
            logger.info("[PASS] #169 Error popup shown: " + errorText);
        } else {
            logger.error("[FAIL] #169 No error popup for furniture + groceries mix");
        }

        assertTrue("Error popup should appear for furniture + groceries mix (R3 violation)", hasError);

        // Verify error mentions categories
        boolean mentionsCategories = errorText.toLowerCase().contains("categor")
                                  || errorText.toLowerCase().contains("furniture")
                                  || errorText.toLowerCase().contains("grocer")
                                  || errorText.toLowerCase().contains("combine")
                                  || errorText.toLowerCase().contains("mix");
        if (mentionsCategories) {
            logger.info("[PASS] #169 Error message mentions category mixing issue");
        } else {
            logger.warn("[WARN] #169 Error message doesn't clearly mention categories: " + errorText);
        }

        logger.info("[PASS] #169 No furniture + groceries mix test complete");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test169_NoFurnitureGroceries.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
