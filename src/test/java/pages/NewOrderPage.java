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

    // ---- Submit / cancel / update / confirm / error popup (⚠ CONFIRM) ---------
    By submitBtn          = By.xpath("(//button|//a)[normalize-space()='Submit Order' or normalize-space()='Submit']");
    By cancelBtn          = By.xpath("(//button|//a)[normalize-space()='Cancel']");
    By updateBtn          = By.xpath("(//button|//a)[normalize-space()='Update']");
    // Confirmation dialog that appears before final submit
    By confirmDialog      = By.xpath("//*[@role='dialog' or @role='alertdialog'][.//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'confirm')]]");
    By confirmBtn         = By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'confirm') or normalize-space()='Yes' or normalize-space()='OK' or normalize-space()='Submit']");
    // Error popup - exclude confirmation dialogs
    By errorPopup         = By.xpath("//*[(@role='alert' or contains(@class,'error') or contains(@class,'toast')) and not(contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'confirm order'))]");

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
    public void click(String xpath) {
        By btn = By.xpath(xpath);
        click(btn);
    }
    /**
     * Click the + button to increase quantity.
     * Tries multiple locator strategies since DOM structure varies.
     */
    public void plus(String productName) {
        By btn = By.xpath(
            // Strategy 1: Button with "+" text near product name
            "//*[.//*[contains(normalize-space(),'" + productName.split(" ")[0] + "')]]//button[normalize-space()='+'] | " +
            // Strategy 2: Button with aria-label for increase
            "//*[.//*[contains(normalize-space(),'" + productName.split(" ")[0] + "')]]//button[contains(@aria-label,'ncrease') or contains(@aria-label,'add') or contains(@aria-label,'plus')] | " +
            // Strategy 3: Any + button in the order/cart section
            "//section[contains(@class,'order') or contains(@class,'cart') or contains(@class,'summary')]//button[normalize-space()='+']"
        );
        click(btn);
    }

    /**
     * Click the - button to decrease quantity.
     */
    public void minus(String productName) {
        By btn = By.xpath(
            "//*[.//*[contains(normalize-space(),'" + productName.split(" ")[0] + "')]]//button[normalize-space()='-'] | " +
            "//*[.//*[contains(normalize-space(),'" + productName.split(" ")[0] + "')]]//button[contains(@aria-label,'ecrease') or contains(@aria-label,'minus') or contains(@aria-label,'subtract')] | " +
            "//section[contains(@class,'order') or contains(@class,'cart') or contains(@class,'summary')]//button[normalize-space()='-']"
        );
        click(btn);
    }

    /**
     * Click trash/remove button for a product.
     */
    public void trash(String productName) {
        By btn = By.xpath(
            "//*[.//*[contains(normalize-space(),'" + productName.split(" ")[0] + "')]]//button[contains(@aria-label,'remove') or contains(@aria-label,'delete') or contains(@class,'trash') or .//svg]"
        );
        click(btn);
    }

    /**
     * Set quantity directly via input field.
     */
    public void setQuantity(String productName, int qty) {
        // Try to find quantity input near the product name
        By input = By.xpath(
            "//*[.//*[contains(normalize-space(),'" + productName.split(" ")[0] + "')]]//input[@type='number'] | " +
            "//input[@type='number'][contains(@aria-label,'qty') or contains(@aria-label,'uantity') or contains(@placeholder,'qty')]"
        );
        if (isPresent(input)) {
            type(input, String.valueOf(qty));
        }
    }

    // ---- Actions: submit / cancel / update -----------------------------------
    public void submit() { click(submitBtn); }
    public void cancel() { click(cancelBtn); }
    public void update() { click(updateBtn); }

    /** Check if confirmation dialog is showing */
    public boolean hasConfirmDialog() { return isPresent(confirmDialog); }

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
        String raw = driver.findElement(By.xpath("/html/body/div/div[1]/main/div/div[5]/div[3]/span[2]")).getText();
        // Remove everything except digits and dots
        String digits = raw.replaceAll("[^0-9.]", "");
        if (digits.isEmpty()) return 0.0;
        // Handle multiple dots (e.g., "1,234.56" -> "1234.56" or text with multiple prices)
        // Keep only the last dot as decimal separator
        int lastDot = digits.lastIndexOf('.');
        if (lastDot >= 0) {
            String beforeDot = digits.substring(0, lastDot).replace(".", "");
            String afterDot = digits.substring(lastDot);
            digits = beforeDot + afterDot;
        }
        try {
            return Double.parseDouble(digits);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
