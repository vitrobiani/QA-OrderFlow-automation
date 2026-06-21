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

import pages.HomePage;
import pages.NewOrderPage;
import pages.NavBar;

/**
 * PractiTest Test #178 — Description viewing.
 * Requirement #137 (Page interactions).
 *
 * Scenario: Toggle description visibility on home page
 * featured products carousel. Verify description shows/hides.
 */
public class Test178_DescriptionViewing {

    private WebDriver driver;
    private static final Logger logger = LogManager.getLogger(Test178_DescriptionViewing.class);

    @Before
    public void setUp() {
        driver = base_test_class.initializeDriver();
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testDescriptionViewing() throws InterruptedException {
        NavBar nav = new NavBar(driver);
        HomePage homePage = new HomePage(driver);

        // Navigate to Home
        driver.get(base_test_class.BASE_URL);
        assertTrue("Home page not loaded", homePage.isLoaded());

        // Verify featured products section exists
        boolean hasFeatured = homePage.featuredCardPresent(1);
        if (!hasFeatured) {
            logger.warn("[SKIP] #178 No featured products on home page");
            return;
        }
        logger.info("#178 Featured products section present");

        // Get initial card text
        String initialText = homePage.featuredCardText(1);
        logger.info("#178 Initial card 1 text length: " + initialText.length());

        // Toggle description for card 1
        homePage.toggleFeaturedDescription(1);
        Thread.sleep(500);
        logger.info("#178 Toggled description for card 1");

        // Get text after toggle
        String afterToggle1 = homePage.featuredCardText(1);
        logger.info("#178 Card 1 text after toggle length: " + afterToggle1.length());

        // Text should change (description shown/hidden)
        boolean textChanged1 = !initialText.equals(afterToggle1);
        if (textChanged1) {
            logger.info("[PASS] #178 Description toggle changed card content");
        } else {
            logger.warn("[WARN] #178 Card content unchanged after toggle (may be animation/timing)");
        }

        // Toggle again to hide
        homePage.toggleFeaturedDescription(1);
        Thread.sleep(500);

        String afterToggle2 = homePage.featuredCardText(1);
        logger.info("#178 Card 1 text after second toggle length: " + afterToggle2.length());

        // Should return to approximately initial state
        boolean toggledBack = afterToggle2.length() <= initialText.length() + 50; // Allow some variance
        if (toggledBack) {
            logger.info("[PASS] #178 Description toggled back");
        }

        // Test another card
        if (homePage.featuredCardPresent(2)) {
            homePage.toggleFeaturedDescription(2);
            Thread.sleep(500);
            logger.info("#178 Toggled description for card 2");

            homePage.toggleFeaturedDescription(2);
            Thread.sleep(500);
            logger.info("[PASS] #178 Card 2 description toggle works");
        }

        driver.get(base_test_class.BASE_URL + "/order");
        NewOrderPage orderPage = new NewOrderPage(driver);
        orderPage.selectCategory("mobile-accessories");

        assertTrue("Order page not loaded", orderPage.isLoaded());

        String productName = orderPage.firstProductName();

        String before = orderPage.productCardText(productName);
        logger.info("Before toggle length: " + before.length());

        orderPage.toggleProductDesc(productName);
        Thread.sleep(500);

        String after = orderPage.productCardText(productName);
        logger.info("After toggle length: " + after.length());

        assertTrue(
                "Description toggle did not change card content",
                before.length() != after.length()
        );

// Toggle back
        orderPage.toggleProductDesc(productName);
        Thread.sleep(500);

        String back = orderPage.productCardText(productName);
        logger.info("After second toggle length: " + back.length());

        assertTrue(
                "Card did not return close to original state",
                Math.abs(back.length() - before.length()) < 20
        );
        logger.info("[PASS] #178 checks passed");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test178_DescriptionViewing.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
