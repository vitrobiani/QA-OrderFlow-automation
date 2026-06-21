package pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
    By inStockToggle      = By.id("in-stock-toggle");
    By priceSlider        = By.xpath("//*[@role='slider' or contains(@class,'slider')]");

    // ---- Submit / cancel / update / error popup (confirmed via DomDiscovery2) -
    By submitBtn          = By.id("btn-submit-order");
    By cancelBtn          = By.id("btn-cancel-order");
    By updateBtn          = By.id("btn-update-order");
    By errorPopup         = By.xpath("//*[@id='validation-dialog' and @data-state='open' and contains(., 'Validation Errors')]");
    By confirmDialog      = By.xpath("//*[@id='validation-dialog' and @data-state='open' and contains(., 'Confirm Order')]");
    By confirmBtn         = By.xpath("//*[@id='validation-dialog']//button[contains(normalize-space(),'Confirm')]");

    // ---- Order summary (confirmed via DomDiscovery2 hot-summary dump) --------
    // Cart rows live in a separate panel as <div id="order-item-<productId>">,
    // each containing decrease-qty-*, quantity-input-*, increase-qty-*, remove-item-*.
    By orderSummaryRows   = By.cssSelector("[id^='order-item-']");
    By orderSumValue      = By.id("order-total");

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
                           + "[.//*[contains(.,'" + productName + "')]]"
                           + "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add')]");
        click(addBtn);
    }

    public void toggleProductDesc(String productName) {
        By descBtn = By.xpath("//*[contains(@class,'product') or contains(@class,'card')]"
                + "[.//*[contains(.,'" + productName + "')]]"
                + "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'description')]");
        click(descBtn);
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

    public void toggleFirstProductDesc() {
        By descBtn = By.xpath("(//*[contains(@class,'product') or contains(@class,'card')]"
                + "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'description')])[1]");
        click(descBtn);
    }

    public String firstProductCardText() {
        By card = By.xpath("(//*[contains(@class,'product') or contains(@class,'card')])[1]");
        return text(card);
    }

    /**
     * Adds the first product whose image is hosted under the given category slug.
     * Needed because this SUT appends new-category products under the previous
     * category's grid instead of replacing it — so `addFirstProduct()` would
     * otherwise pick a stale top-of-grid card from the old category.
     */
    public void addFirstProductFromCategory(String categorySlug) {
        By btn = By.xpath(
            "(//*[starts-with(@data-testid,'product-card-')]"
          + "[.//img[contains(@src,'/" + categorySlug + "/')]]"
          + "//button[starts-with(@data-testid,'select-product-')])[1]"
        );
        click(btn);
    }

    /**
     * Returns the title of the first product card whose image is hosted under
     * the given category slug.
     */
    public String firstProductNameFromCategory(String categorySlug) {
        By title = By.xpath(
            "(//*[starts-with(@data-testid,'product-card-')]"
          + "[.//img[contains(@src,'/" + categorySlug + "/')]]"
          + "//h3)[1]"
        );
        return text(title);
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

    public String productCardText(String productName) {
        By card = By.xpath(
                "//*[contains(@class,'product') or contains(@class,'card')]"
                        + "[.//*[contains(.,'" + productName + "')]]"
        );
        return text(card);
    }

    /**
     * Returns the stock count for the first product card.
     * Stock label format: "47 in stock" → 47.
     */
    public int firstProductStock() {
        By firstStock = By.cssSelector("[data-testid^='stock-']");
        String raw = text(firstStock);
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(raw);
        return m.find() ? Integer.parseInt(m.group()) : 0;
    }

    /**
     * Returns the price of the first product card.
     * Price format: "$123.45" → 123.45
     * Tries multiple locator strategies to find the price.
     */
    public double firstProductPrice() {
        // Try different locator strategies
        By[] priceLocators = {
            By.cssSelector("[data-testid^='price-']"),
            By.xpath("(//*[contains(@class,'product') or contains(@class,'card')]//*[starts-with(normalize-space(),'$')])[1]"),
            By.xpath("(//*[contains(@class,'product') or contains(@class,'card')]//*[contains(@class,'price')])[1]"),
            By.xpath("(//*[contains(@data-testid,'price')])[1]")
        };

        for (By locator : priceLocators) {
            if (isPresent(locator)) {
                String raw = text(locator);
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+\\.\\d{2}").matcher(raw);
                if (m.find()) {
                    return Double.parseDouble(m.group());
                }
            }
        }
        return 0.0;
    }

    /**
     * Returns the quantity for a product in the order summary.
     */
    public int getQuantity(String productName) {
        By input = By.xpath(orderItemXPath(productName) + "//input[starts-with(@id,'quantity-input-')]");
        if (isPresent(input)) {
            String val = waitVisible(input).getAttribute("value");
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }
    public void plus(String productName)  { click(qtyButton(productName, "increase-qty-")); }
    public void minus(String productName) { click(qtyButton(productName, "decrease-qty-")); }
    public void trash(String productName) { click(qtyButton(productName, "remove-item-")); }
    public void setQuantity(String productName, int qty) {
        By input = By.xpath(orderItemXPath(productName) + "//input[starts-with(@id,'quantity-input-')]");
        if (isPresent(input)) {
            type(input, String.valueOf(qty));
        }
    }
    private String orderItemXPath(String productName) {
        return "//*[starts-with(@id,'order-item-')][.//*[normalize-space()='" + productName + "']]";
    }
    private By qtyButton(String productName, String idPrefix) {
        return By.xpath(orderItemXPath(productName) + "//button[starts-with(@id,'" + idPrefix + "')]");
    }

    // ---- Actions: submit / cancel / update -----------------------------------
    public void submit()  { click(submitBtn); }
    public void cancel()  { click(cancelBtn); }
    public void update()  { click(updateBtn); }
    public void confirm() { click(confirmBtn); }
    public boolean hasConfirmDialog() { return isPresent(confirmDialog); }

    /** Check if confirmation dialog is showing */
//    public boolean hasConfirmDialog() { return isPresent(confirmDialog); }

    /** Click confirm on the confirmation dialog */
    public void confirmOrder() {
        if (isPresent(confirmBtn)) {
            click(confirmBtn);
        }
    }

    /** Submit and confirm (handles the two-step flow) */
    public void submitAndConfirm() {
        click(submitBtn);
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        // Try to find and click confirm button if dialog appears
        for (int i = 0; i < 3; i++) {
            if (isPresent(confirmBtn)) {
                click(confirmBtn);
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                break;
            }
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
    }

    // ---- Reads ---------------------------------------------------------------
    public boolean isLoaded()              { return isPresent(heading); }
    public boolean hasProductsEmptyState() { return isPresent(productsEmptyState); }
    public boolean hasSummaryEmptyState()  { return isPresent(summaryEmptyState); }
    public boolean hasError()              { return isPresent(errorPopup); }
    public String  errorText()             { return text(errorPopup); }
    public List<WebElement> summaryRows()  { return findAll(orderSummaryRows); }
    public double orderSum() {
        String raw = text(orderSumValue);
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+(?:\\.\\d+)?").matcher(raw);
        String last = null;
        while (m.find()) last = m.group();
        return last == null ? 0.0 : Double.parseDouble(last);
    }
}
