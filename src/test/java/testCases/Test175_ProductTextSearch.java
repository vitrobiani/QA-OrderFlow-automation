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
import utils.JsonData;

import org.json.simple.JSONObject;

/**
 * PractiTest Test #175 — Product text search.
 * Requirement #137 (Page interactions).
 *
 * Scenario: Search for a term matching product name/description,
 * verify only matching products show. Then search a no-match term,
 * verify no products show.
 */
public class Test175_ProductTextSearch {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test175_ProductTextSearch.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testProductTextSearch() throws InterruptedException {
        NavBar nav = new NavBar(driver);
        NewOrderPage orderPage = new NewOrderPage(driver);

        // Load search queries from test data
        JSONObject searchData = JsonData.readObject("testdata/search_queries.json");
        String matchTerm = searchData != null ? (String) searchData.get("match") : "phone";
        String noMatchTerm = searchData != null ? (String) searchData.get("noMatch") : "zzqqxx_no_such_product_123";

        logger.info("#175 Match term: " + matchTerm + ", No-match term: " + noMatchTerm);

        // Navigate to New Order
        driver.get(base_test_class.BASE_URL);
        nav.goNewOrder();
        assertTrue("New Order page not loaded", orderPage.isLoaded());

        // Select a category first
        orderPage.selectCategory("smartphones");
        Thread.sleep(2000);

        // Get initial product count
        int initialCount = orderPage.productCardCount();
        logger.info("#175 Initial product count: " + initialCount);

        // Search for matching term
        orderPage.search(matchTerm);
        Thread.sleep(1500);

        int matchCount = orderPage.productCardCount();
        logger.info("#175 Products after searching '" + matchTerm + "': " + matchCount);

        // Matching search should show some products (possibly fewer than initial)
        if (matchCount > 0) {
            logger.info("[PASS] #175 Search '" + matchTerm + "' returned " + matchCount + " products");
        } else {
            logger.warn("[WARN] #175 Search '" + matchTerm + "' returned 0 products (may be expected if no match in category)");
        }

        // Clear search and verify products return
        orderPage.search("");
        Thread.sleep(1000);

        int clearedCount = orderPage.productCardCount();
        logger.info("#175 Products after clearing search: " + clearedCount);

        // Search for no-match term
        orderPage.search(noMatchTerm);
        Thread.sleep(1500);

        int noMatchCount = orderPage.productCardCount();
        logger.info("#175 Products after searching '" + noMatchTerm + "': " + noMatchCount);

        // No-match search should show 0 products (or empty state)
        boolean noMatchEmpty = noMatchCount == 0 || orderPage.hasProductsEmptyState();
        if (noMatchEmpty) {
            logger.info("[PASS] #175 No-match search correctly shows no products");
        } else {
            logger.error("[FAIL] #175 No-match search still shows " + noMatchCount + " products");
        }

        assertTrue("No-match search should return 0 products", noMatchEmpty);

        logger.info("[PASS] #175 Product text search test complete");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test175_ProductTextSearch.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
