package pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * /order — the heart of the app.
 *
 * Confirmed locators (session-1 discovery, cold state):
 *   - Page heading: <h1>New Order</h1>
 *   - Category control is a native <select id="category-select"> whose option
 *     values match the API slugs ("furniture", "groceries", "laptops",
 *     "mobile-accessories", "smartphones", "sports-accessories"). So we can
 *     drive it cleanly with Selenium's Select helper and the slug from
 *     testdata/categories.json — no slug↔label translation needed.
 *   - Pre-selection empty state: id="products-empty" with text
 *     "Select a category to browse products."
 *   - Empty order summary text: "No products selected yet. Browse and add products above."
 *
 * Locators still ⚠ pending a second discovery pass (with a category selected, with
 * an in-progress order, and with a validation error open):
 *   - product cards + their Add / qty controls
 *   - search input, in-stock toggle, price slider
 *   - order summary rows + running total
 *   - Submit / Cancel / Update buttons
 *   - validation error popup (modal vs toast)
 */
public class NewOrderPage extends BasePage {

    public NewOrderPage(WebDriver driver) { super(driver); }

    // ---- Page-level locators (confirmed) -------------------------------------
    By heading            = By.xpath("//main//h1[normalize-space()='New Order']");
    By categorySelect     = By.id("category-select");
    By productsEmptyState = By.id("products-empty");
    By summaryEmptyState  = By.xpath("//main//*[contains(normalize-space(),'No products selected yet')]");

    // ---- Filters / sort / search (⚠ CONFIRM after second discovery pass) -----
    By searchInput        = By.xpath("//input[@type='search' or @type='text'][contains(@placeholder,'earch') or contains(@aria-label,'earch')]");
    By inStockToggle      = By.xpath("//*[self::input or self::button or @role='switch'][contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'in stock')]");
    By priceSlider        = By.xpath("//*[@role='slider' or contains(@class,'slider')]");

    // ---- Submit / cancel / update / error popup (⚠ CONFIRM) ------------------
    By submitBtn          = By.xpath("(//button|//a)[normalize-space()='Submit Order' or normalize-space()='Submit']");
    By cancelBtn          = By.xpath("(//button|//a)[normalize-space()='Cancel']");
    By updateBtn          = By.xpath("(//button|//a)[normalize-space()='Update']");
    By errorPopup         = By.xpath("//*[@role='alertdialog' or @role='dialog' or contains(@class,'error') or contains(@class,'toast')]");

    // ---- Order summary (⚠ CONFIRM) -------------------------------------------
    By orderSummaryRows   = By.cssSelector("[data-testid^='summary-row-']");
    By orderSumValue      = By.xpath("//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'total') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sum')]");

    // ---- Actions: category / search / filters -------------------------------
    /**
     * Pick a category by slug (matches the <option value="..."> attrs and the
     * testdata/categories.json entries). Use Selenium's Select helper since
     * the control is a native <select>.
     */
    public void selectCategory(String slug) {
        Select sel = new Select(waitVisible(categorySelect));
        sel.selectByValue(slug);
    }
    public String currentCategory() {
        return new Select(waitVisible(categorySelect)).getFirstSelectedOption().getAttribute("value");
    }

    public void search(String term)         { type(searchInput, term); }
    public void toggleInStockOnly()         { click(inStockToggle); }

    // ---- Actions: add / adjust / remove (on-the-fly per course pattern) ------
    // These follow the course's dynamic-locator concatenation idea; exact testid
    // patterns will be locked after the second discovery pass.
    public void addProduct(String productName) {
        By addBtn = By.xpath("//*[contains(@class,'product') or contains(@class,'card')]"
                           + "[.//*[normalize-space()='" + productName + "']]"
                           + "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add')]");
        click(addBtn);
    }

    /**
     * Adds the first available product in the current category.
     * Useful when we don't know product names ahead of time.
     */
    public void addFirstProduct() {
        By firstAddBtn = By.xpath("(//*[contains(@class,'product') or contains(@class,'card')]"
                                + "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add')])[1]");
        click(firstAddBtn);
    }

    /**
     * Returns the name/title of the first product card displayed.
     */
    public String firstProductName() {
        By firstTitle = By.xpath("(//*[contains(@class,'product') or contains(@class,'card')]//*[contains(@class,'title') or self::h3 or self::h4])[1]");
        return text(firstTitle);
    }

    /**
     * Returns the count of visible product cards.
     */
    public int productCardCount() {
        return findAll(By.xpath("//*[contains(@class,'product') or contains(@class,'card')][.//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add')]]")).size();
    }
    public void plus(String productName)  { click(summaryRowButton(productName, "+")); }
    public void minus(String productName) { click(summaryRowButton(productName, "-")); }
    public void trash(String productName) {
        By btn = By.xpath(summaryRowXPath(productName)
                + "//button[contains(@aria-label,'remove') or contains(@aria-label,'delete') or contains(@class,'trash')]");
        click(btn);
    }
    public void setQuantity(String productName, int qty) {
        By input = By.xpath(summaryRowXPath(productName)
                + "//input[@type='number' or contains(@aria-label,'qty') or contains(@aria-label,'quantity')]");
        if (isPresent(input)) {
            type(input, String.valueOf(qty));
        }
    }
    private String summaryRowXPath(String productName) {
        return "//*[contains(@class,'summary') or contains(@class,'cart') or contains(@class,'order-line')]"
             + "[.//*[normalize-space()='" + productName + "']]";
    }
    private By summaryRowButton(String productName, String label) {
        return By.xpath(summaryRowXPath(productName) + "//button[normalize-space()='" + label + "']");
    }

    // ---- Actions: submit / cancel / update -----------------------------------
    public void submit() { click(submitBtn); }
    public void cancel() { click(cancelBtn); }
    public void update() { click(updateBtn); }

    // ---- Reads ---------------------------------------------------------------
    public boolean isLoaded()              { return isPresent(heading); }
    public boolean hasProductsEmptyState() { return isPresent(productsEmptyState); }
    public boolean hasSummaryEmptyState()  { return isPresent(summaryEmptyState); }
    public boolean hasError()              { return isPresent(errorPopup); }
    public String  errorText()             { return text(errorPopup); }
    public List<WebElement> summaryRows()  { return findAll(orderSummaryRows); }
    public double orderSum() {
        String raw = text(orderSumValue);
        String digits = raw.replaceAll("[^0-9.]", "");
        return digits.isEmpty() ? 0.0 : Double.parseDouble(digits);
    }
}
