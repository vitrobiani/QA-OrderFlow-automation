package testCases;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import api.DummyJsonClient;
import io.restassured.response.Response;
import utils.JsonData;

/**
 * PractiTest Test #161 — API product fetch (REST Assured).
 *
 * For every category in testdata/categories.json:
 *   GET https://dummyjson.com/products/category/{cat}?limit=10
 * must return 200 and every product object must expose
 *   title, description, images, price, stock
 * (the descriptors the spec says the UI then renders per product card).
 *
 * No browser is needed — this is the cheapest, most stable test in the suite, so we
 * keep it as the first thing the user can run end-to-end.
 */
public class Test161_ApiProductFetch {

    private static final Logger logger = LogManager.getLogger(Test161_ApiProductFetch.class);

    @Test
    public void apiProductFetch() {
        List<String> categories = JsonData.readStringArray("testdata/categories.json");
        DummyJsonClient client = new DummyJsonClient();

        for (String cat : categories) {
            Response res = client.getCategory(cat);

            // Status -----------------------------------------------------------
            int status = res.getStatusCode();
            if (status != 200) {
                logger.error("[FAIL] #161 {} -> HTTP {}", cat, status);
            }
            assertEquals("status for " + cat, 200, status);

            // At least one product -------------------------------------------
            List<Object> products = res.jsonPath().getList("products");
            assertNotNull(cat + " missing 'products' array", products);
            assertTrue(cat + " returned zero products", products.size() > 0);

            // Every product must expose the five spec descriptors -----------
            List<String> titles       = res.jsonPath().getList("products.title");
            List<String> descriptions = res.jsonPath().getList("products.description");
            List<Object> imagesLists  = res.jsonPath().getList("products.images");
            List<Object> prices       = res.jsonPath().getList("products.price");
            List<Object> stocks       = res.jsonPath().getList("products.stock");

            assertEquals(cat + " titles count",       products.size(), titles.size());
            assertEquals(cat + " descriptions count", products.size(), descriptions.size());
            assertEquals(cat + " images count",       products.size(), imagesLists.size());
            assertEquals(cat + " prices count",       products.size(), prices.size());
            assertEquals(cat + " stocks count",       products.size(), stocks.size());

            for (int i = 0; i < products.size(); i++) {
                assertNotNull(cat + " product[" + i + "] missing title",       titles.get(i));
                assertNotNull(cat + " product[" + i + "] missing description", descriptions.get(i));
                assertNotNull(cat + " product[" + i + "] missing images",      imagesLists.get(i));
                assertNotNull(cat + " product[" + i + "] missing price",       prices.get(i));
                assertNotNull(cat + " product[" + i + "] missing stock",       stocks.get(i));
            }

            logger.info("[PASS] #161 {} -> {} products, all descriptors present", cat, products.size());
        }

        logger.info("[PASS] #161 API product fetch — all categories OK");
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        org.junit.runner.Result result = junit.run(Test161_ApiProductFetch.class);
        if (result.getFailureCount() > 0) { System.out.println("Test failed."); System.exit(1); }
        else { System.out.println("Test finished successfully."); System.exit(0); }
    }
}
