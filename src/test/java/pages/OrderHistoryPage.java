package pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * /history — list of submitted orders.
 *
 * Confirmed (session-1 discovery, cold state):
 *   - Page heading: <h1>Order History</h1>
 *   - Cold-state subtitle: "0 orders this session"
 *   - Cold-state empty body: "No orders yet. Create your first order!"
 *   - No Export CSV button appears in cold state (matches spec #170).
 *
 * ⚠ pending hot-state discovery: per-order row structure, Export CSV button
 * locator + behavior (real download vs in-page render — see LOCATORS.md Q2).
 */
public class OrderHistoryPage extends BasePage {

    public OrderHistoryPage(WebDriver driver) { super(driver); }

    // ---- Page-level locators (confirmed) -------------------------------------
    By heading        = By.xpath("//main//h1[normalize-space()='Order History']");
    By subtitle       = By.xpath("//main//p[contains(.,'orders this session')]");
    By emptyState     = By.xpath("//main//p[contains(.,'No orders yet')]");

    // ---- Hot-state locators (⚠ CONFIRM after hot discovery pass) -------------
    By orderRows      = By.cssSelector("[data-testid^='order-row-']");
    By exportBtn      = By.xpath("(//button|//a)[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'export') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'csv')]");

    // ---- Actions / reads -----------------------------------------------------
    public boolean isLoaded()           { return isPresent(heading); }
    public boolean isEmpty()            { return isPresent(emptyState); }
    public boolean hasExportButton()    { return isPresent(exportBtn); }
    public String  subtitleText()       { return text(subtitle); }
    public void    clickExport()        { click(exportBtn); }
    public List<WebElement> rows()      { return findAll(orderRows); }

    // On-the-fly: does any order row in history contain this product name?
    public boolean containsProduct(String productName) {
        return isPresent(By.xpath("//main//*[normalize-space()='" + productName + "']"));
    }
}
