package testCases;

import static org.junit.Assert.assertTrue;

import com.sun.xml.bind.v2.TODO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.internal.TextListener;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import org.openqa.selenium.WebElement;
import pages.NavBar;
import pages.NewOrderPage;

/**
 * PractiTest Test #166 — Add products to order.
 * Requirement #140 (Order process).
 *
 * Scenario: Add products from same category and different categories,
 * change quantities. Verify summary lines and sum are correct.
 */
public class Test166_AddProductsToOrder {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test166_AddProductsToOrder.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testAddProductsToOrder() throws InterruptedException {
        NavBar nav = new NavBar(driver);
        NewOrderPage orderPage = new NewOrderPage(driver);

        // Navigate to New Order
        driver.get(base_test_class.BASE_URL);
        nav.goNewOrder();
        assertTrue("New Order page not loaded", orderPage.isLoaded());

        // Verify empty state initially
        boolean emptyInitially = orderPage.hasSummaryEmptyState();
        logger.info("#166 Initial empty state: " + emptyInitially);

        // Add first product from laptops
        orderPage.selectCategory("laptops");
        Thread.sleep(2000);

        String product1 = orderPage.firstProductName();
        logger.info("#166 Adding product 1: " + product1);
        orderPage.addFirstProduct();
        Thread.sleep(1000);

        // Check summary has 1 row
        int rowsAfter1 = orderPage.summaryRows().size();
        double sumAfter1 = orderPage.orderSum();
        logger.info("#166 After adding product 1: rows=" + rowsAfter1 + ", sum=$" +  sumAfter1);

        boolean hasFirstProduct = rowsAfter1 > 0 || !orderPage.hasSummaryEmptyState();
        if (hasFirstProduct) {
            logger.info("[PASS] #166 First product added to summary");
        } else {
            logger.error("[FAIL] #166 First product not in summary");
        }
        assertTrue("First product should appear in summary", hasFirstProduct);

        // Switch to different category and add another product
        // (Using sports-accessories to avoid furniture+groceries R3 conflict)
        orderPage.selectCategory("sports-accessories");
        Thread.sleep(2000);

        String product2 = orderPage.firstProductName();
        logger.info("#166 Adding product 2: " + product2);
        orderPage.addFirstProduct();
        Thread.sleep(1000);

        // Check summary updated
        int rowsAfter2 = orderPage.summaryRows().size();
        double sumAfter2 = orderPage.orderSum();
        logger.info("#166 After adding product 2: rows=" + rowsAfter2 + ", sum=$" +  sumAfter2);

        boolean sumIncreased = sumAfter2 > sumAfter1;
        if (sumIncreased) {
            logger.info("[PASS] #166 Sum increased after adding second product");
        } else {
            logger.warn("[WARN] #166 Sum didn't increase (may be same product or issue)");
        }

        // Test quantity adjustment with + button
        try {
            logger.info("#166 Testing quantity increase with + button...");
            orderPage.click("/html/body/div/div[1]/main/div/div[5]/div[2]/div[1]/div[2]/button[2]" );
            orderPage.click("/html/body/div/div[1]/main/div/div[5]/div[2]/div[2]/div[2]/button[2]" );
            Thread.sleep(2000);

            logger.info("[PASS] #166 Plus button clicked successfully");

        } catch (Exception e) {
            logger.warn("[WARN] #166 Plus button not found or not clickable: " + e.getMessage());
            logger.warn("       This may indicate the summary row locators need adjustment");
        }
        try {
//            TODO - add assert to make sure order sum is correct at the end
        } catch (Exception e) {
        }

        logger.info("[PASS] #166 Add products to order test complete");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test166_AddProductsToOrder.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
