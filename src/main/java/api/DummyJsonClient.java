package api;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * Tiny REST Assured client for the dummyjson backing API.
 * Matches the style of the course's `targil_rest_assured` project.
 */
public class DummyJsonClient {

    public static final String BASE = "https://dummyjson.com";

    /** GET /products/category/{category}?limit={limit} */
    public Response getCategory(String category, int limit) {
        return RestAssured
                .given()
                .baseUri(BASE)
                .when()
                .get("/products/category/" + category + "?limit=" + limit);
    }

    /** Default limit 10 (per the spec). */
    public Response getCategory(String category) {
        return getCategory(category, 10);
    }
}
