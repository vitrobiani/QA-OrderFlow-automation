package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Home / landing page (/).
 *
 * Confirmed structure (session-1 DOM discovery):
 *   - Welcome section: data-testid="welcome-section", heading "Welcome to OrderFlow"
 *   - Quick Navigation cards (the spec's "carousel"): nav-card-new-order /
 *     nav-card-order-history / nav-card-returns — these live in NavBar
 *   - System Overview stats: stat-total-products / stat-categories /
 *     stat-orders-processed / stat-return-requests
 *   - Featured Products carousel: featured-product-1..4 with featured-desc-btn-1..4
 *     "Product description" toggles — drives #178.
 */
public class HomePage extends BasePage {

    public HomePage(WebDriver driver) { super(driver); }

    // ---- Page-level locators -------------------------------------------------
    By welcomeSection       = By.cssSelector("[data-testid='welcome-section']");
    By navigationSection    = By.cssSelector("[data-testid='navigation-section']");
    By statisticsSection    = By.cssSelector("[data-testid='statistics-section']");
    By featuredSection      = By.cssSelector("[data-testid='featured-products-section']");
    By aboutSection         = By.cssSelector("[data-testid='about-section']");

    // ---- Stats (cold-state assertions) ---------------------------------------
    By statTotalProducts    = By.cssSelector("[data-testid='stat-total-products']");
    By statCategories       = By.cssSelector("[data-testid='stat-categories']");
    By statOrdersProcessed  = By.cssSelector("[data-testid='stat-orders-processed']");
    By statReturnRequests   = By.cssSelector("[data-testid='stat-return-requests']");

    // ---- Actions / reads -----------------------------------------------------
    public boolean isLoaded()                { return isPresent(welcomeSection); }
    public String  totalProductsText()       { return text(statTotalProducts); }
    public String  categoriesText()          { return text(statCategories); }
    public String  ordersProcessedText()     { return text(statOrdersProcessed); }
    public String  returnRequestsText()      { return text(statReturnRequests); }

    // ---- Featured-product description toggle (#178) --------------------------
    // Cards are numbered 1..4. The toggle button has testid featured-desc-btn-{n}
    // and the card itself is featured-product-{n}.
    public void toggleFeaturedDescription(int cardNumber) {
        click(By.cssSelector("[data-testid='featured-desc-btn-" + cardNumber + "']"));
    }
    public boolean featuredCardPresent(int cardNumber) {
        return isPresent(By.cssSelector("[data-testid='featured-product-" + cardNumber + "']"));
    }
    public String featuredCardText(int cardNumber) {
        return text(By.cssSelector("[data-testid='featured-product-" + cardNumber + "']"));
    }
}
