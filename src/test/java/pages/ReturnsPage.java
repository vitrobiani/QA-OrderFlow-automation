package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

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

    // ---- Hot-state form (⚠ CONFIRM after hot discovery) ----------------------
    By productDropdown= By.xpath("//main//select | //main//*[@role='combobox']");
    By qtyInput       = By.xpath("//main//input[@type='number' or contains(@aria-label,'qty') or contains(@aria-label,'quantity') or contains(@placeholder,'qty') or contains(@placeholder,'Quantity')]");
    By submitBtn      = By.xpath("//main//*[self::button or self::a][normalize-space()='Submit Return' or normalize-space()='Submit' or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'return')]");
    By errorMessage   = By.xpath("//*[@role='alert' or @role='alertdialog' or contains(@class,'error')]");
    By successMessage = By.xpath("//*[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'success') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'returned')]");

    // ---- Actions / reads -----------------------------------------------------
    public boolean isLoaded()                       { return isPresent(heading); }
    public boolean hasColdEmptyState()              { return isPresent(coldEmptyState); }
    public void    selectProduct(String name) {
        click(productDropdown);
        click(By.xpath("//*[normalize-space()='" + name + "']"));
    }
    public void    setQty(int n)                    { type(qtyInput, String.valueOf(n)); }
    public void    submit()                         { click(submitBtn); }
    public boolean hasError()                       { return isPresent(errorMessage); }
    public String  errorText()                      { return text(errorMessage); }
    public boolean hasSuccess()                     { return isPresent(successMessage); }
}
