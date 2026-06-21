package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * /returns — return form: pick a product, enter return qty, submit.
 *
 * Confirmed (session-1 discovery, cold state):
 *   - Page heading: <h1>Product Returns</h1>
 *   - Cold-state body: "No confirmed orders to return products from." (no form rendered)
 *
 * ⚠ pending hot-state discovery: product dropdown, qty input, submit button,
 * success / error messaging. Also Q3 in LOCATORS.md: what happens for #158
 * (returning a product / order that doesn't exist).
 */
public class ReturnsPage extends BasePage {

    public ReturnsPage(WebDriver driver) { super(driver); }

    // ---- Page-level locators (confirmed) -------------------------------------
    By heading        = By.xpath("//main//h1[normalize-space()='Product Returns']");
    By coldEmptyState = By.xpath("//main//p[contains(.,'No confirmed orders to return')]");

    // ---- Hot-state form (confirmed via hot-returns dump) ---------------------
    // Form starts with only the product <select>. After a product is picked,
    // qty input + submit button render. Their exact locators are still ⚠ —
    // re-dump after select to confirm.
    By productDropdown= By.id("return-product-select");
    By qtyInput       = By.xpath("//main//input[@type='number']");
    By submitBtn      = By.xpath("//main//button[contains(normalize-space(),'Return') or contains(normalize-space(),'Submit')]");
    By errorMessage   = By.xpath("//*[@role='alert' or @role='alertdialog' or contains(@class,'error')]");
    By successMessage = By.xpath("//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'success') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'returned')]");

    // ---- Actions / reads -----------------------------------------------------
    public boolean isLoaded()                       { return isPresent(heading); }
    public boolean hasColdEmptyState()              { return isPresent(coldEmptyState); }
    public void    selectProduct(String name) {
        // Options look like: "iPad Mini 2021 Starlight (ordered: 1)" — startsWith match.
        Select sel = new Select(waitVisible(productDropdown));
        for (WebElement opt : sel.getOptions()) {
            String t = opt.getText();
            if (!t.isEmpty() && t.startsWith(name)) {
                sel.selectByVisibleText(t);
                return;
            }
        }
        // Fallback: pick the first non-placeholder option.
        sel.selectByIndex(1);
    }
    public void    setQty(int n)                    { type(qtyInput, String.valueOf(n)); }
    /**
     * Returns true if a product whose option text starts with the given name
     * is still listed in the returns dropdown.
     * Used to verify a fully-returned product disappears from the dropdown so
     * you cannot return the same product twice (which would inflate stock).
     */
    public boolean isProductInDropdown(String name) {
        Select sel = new Select(waitVisible(productDropdown));
        for (WebElement opt : sel.getOptions()) {
            String t = opt.getText();
            if (!t.isEmpty() && t.startsWith(name)) return true;
        }
        return false;
    }
    public void    submit()                         { click(submitBtn); }
    public boolean hasError()                       { return isPresent(errorMessage); }
    public String  errorText()                      { return text(errorMessage); }
    public boolean hasSuccess()                     { return isPresent(successMessage); }
}
