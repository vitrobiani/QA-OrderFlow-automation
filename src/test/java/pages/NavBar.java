package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Top-bar / global navigation. Used by tests #153, #154, #155, #156, #157.
 *
 * Locators confirmed against the live DOM (see LOCATORS.md, session-1 discovery):
 *   - Nav links carry stable ids: #nav-home / #nav-new-order / #nav-order-history / #nav-returns.
 *     They appear identically on every page (sticky header).
 *   - Home page additionally has "Quick Navigation" cards used as the spec's
 *     "carousel quick-nav" — testids nav-card-new-order / nav-card-order-history / nav-card-returns.
 *   - URL-fragment assertions are the most reliable "did we land?" check.
 *   - ⚠ "Logo" — header brand "OrderFlow" is not wrapped in an anchor and has no visible
 *     click handler in static HTML. clickLogo() targets the brand text; if it turns out
 *     to be non-clickable, Test #157 will surface that as a finding (see LOCATORS.md).
 */
public class NavBar extends BasePage {

    public NavBar(WebDriver driver) { super(driver); }

    // ---- Route fragments (the on-which-screen check) -------------------------
    public static final String ROUTE_HOME    = "/";
    public static final String ROUTE_ORDER   = "/order";
    public static final String ROUTE_HISTORY = "/history";
    public static final String ROUTE_RETURNS = "/returns";

    // ---- Locators (confirmed) ------------------------------------------------
    By homeLink         = By.id("nav-home");
    By newOrderLink     = By.id("nav-new-order");
    By historyLink      = By.id("nav-order-history");
    By returnsLink      = By.id("nav-returns");

    // Home page "Quick Navigation" cards — the spec's "carousel quick-nav".
    By cardNewOrder     = By.cssSelector("[data-testid='nav-card-new-order']");
    By cardHistory      = By.cssSelector("[data-testid='nav-card-order-history']");
    By cardReturns      = By.cssSelector("[data-testid='nav-card-returns']");

    // Brand / "logo" — sits inside <header> as a div+span. Not an <a>; click may no-op. ⚠
    By logoBrand        = By.xpath("//header//span[normalize-space()='OrderFlow']");

    // ---- Actions -------------------------------------------------------------
    public void goHome()          { click(homeLink); }
    public void goNewOrder()      { click(newOrderLink); }
    public void goOrderHistory()  { click(historyLink); }
    public void goReturns()       { click(returnsLink); }
    public void clickLogo()       { click(logoBrand); }

    public void cardNewOrder()    { click(cardNewOrder); }
    public void cardHistory()     { click(cardHistory); }
    public void cardReturns()     { click(cardReturns); }

    // ---- Screen assertions (URL-fragment based) ------------------------------
    public boolean isOnHome() {
        String u = driver.getCurrentUrl().toLowerCase().replaceAll("[?#].*$","");
        return u.endsWith("/") || u.endsWith(".app");
    }
    public boolean isOnNewOrder()  { return driver.getCurrentUrl().toLowerCase().contains(ROUTE_ORDER); }
    public boolean isOnHistory()   { return driver.getCurrentUrl().toLowerCase().contains(ROUTE_HISTORY); }
    public boolean isOnReturns()   { return driver.getCurrentUrl().toLowerCase().contains(ROUTE_RETURNS); }
}
