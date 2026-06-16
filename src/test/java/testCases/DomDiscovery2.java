package testCases;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Session 2.5 hot-state DOM discovery.
 *
 * Unlike DomDiscovery (cold state), this utility:
 *   1. Selects a category and captures product card DOM
 *   2. Adds a product to order and captures summary DOM
 *   3. Triggers each validation rule (R1/R2/R3) and captures error DOM
 *   4. Submits a valid order, goes to history, captures export button DOM
 *
 * Run via `mvn -q -Dtest=DomDiscovery2 test` or main().
 * Output goes to discovery/hot-*.html and discovery/hot-*.elements.txt
 */
public class DomDiscovery2 {

    public static void main(String[] args) throws Exception {
        File out = new File("discovery");
        if (!out.exists()) out.mkdirs();

        WebDriver driver = base_test_class.initializeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            System.out.println("=== Hot-state Discovery Session 2.5 ===\n");

            // 1. Category selected + product cards
            System.out.println("1. Discovering product cards after category selection...");
            driver.get(base_test_class.BASE_URL + "/order");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("category-select")));
            Thread.sleep(1000);

            Select cat = new Select(driver.findElement(By.id("category-select")));
            cat.selectByValue("laptops");
            Thread.sleep(2000);

            dumpHtml(driver, new File(out, "hot-products.html"));
            dumpElements(driver, new File(out, "hot-products.elements.txt"));
            System.out.println("  -> discovery/hot-products.*");

            // 2. Product added to order + summary row
            System.out.println("2. Discovering order summary after adding product...");
            // Click first "Add" button
            List<WebElement> addBtns = driver.findElements(By.xpath(
                    "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add')]"));
            if (!addBtns.isEmpty()) {
                addBtns.get(0).click();
                Thread.sleep(1500);
            }
            dumpHtml(driver, new File(out, "hot-summary.html"));
            dumpElements(driver, new File(out, "hot-summary.elements.txt"));
            System.out.println("  -> discovery/hot-summary.*");

            // 3. Trigger validation error (qty > stock)
            System.out.println("3. Discovering validation error popup...");
            List<WebElement> qtyInputs = driver.findElements(By.xpath(
                    "//input[@type='number']"));
            if (!qtyInputs.isEmpty()) {
                qtyInputs.get(0).clear();
                qtyInputs.get(0).sendKeys("99999");
                Thread.sleep(500);
                // Click submit to trigger error
                List<WebElement> submitBtns = driver.findElements(By.xpath(
                        "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'submit')]"));
                if (!submitBtns.isEmpty()) {
                    submitBtns.get(0).click();
                    Thread.sleep(1500);
                }
            }
            dumpHtml(driver, new File(out, "hot-error.html"));
            dumpElements(driver, new File(out, "hot-error.elements.txt"));
            System.out.println("  -> discovery/hot-error.*");

            // 4. Submit valid order and check history + export
            System.out.println("4. Submitting valid order, then checking history/export...");
            driver.get(base_test_class.BASE_URL + "/order");
            Thread.sleep(1000);
            cat = new Select(driver.findElement(By.id("category-select")));
            cat.selectByValue("groceries");
            Thread.sleep(2000);

            addBtns = driver.findElements(By.xpath(
                    "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add')]"));
            if (!addBtns.isEmpty()) {
                addBtns.get(0).click();
                Thread.sleep(1000);
            }

            List<WebElement> submitBtns = driver.findElements(By.xpath(
                    "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'submit')]"));
            if (!submitBtns.isEmpty()) {
                submitBtns.get(0).click();
                Thread.sleep(2000);
            }

            // Go to history
            driver.findElement(By.id("nav-order-history")).click();
            Thread.sleep(2000);

            dumpHtml(driver, new File(out, "hot-history.html"));
            dumpElements(driver, new File(out, "hot-history.elements.txt"));
            System.out.println("  -> discovery/hot-history.*");

            // 5. Check returns page in hot state (with order to return)
            System.out.println("5. Discovering returns page in hot state...");
            driver.findElement(By.id("nav-returns")).click();
            Thread.sleep(2000);

            dumpHtml(driver, new File(out, "hot-returns.html"));
            dumpElements(driver, new File(out, "hot-returns.elements.txt"));
            System.out.println("  -> discovery/hot-returns.*");

            System.out.println("\n=== Hot-state discovery complete ===");
            System.out.println("Review the discovery/hot-*.elements.txt files to confirm locators.");

        } finally {
            driver.quit();
        }
    }

    private static void dumpHtml(WebDriver driver, File f) throws Exception {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(f))) {
            w.write(driver.getPageSource());
        }
    }

    private static void dumpElements(WebDriver driver, File f) throws Exception {
        List<WebElement> all = driver.findElements(By.cssSelector(
                "button, a, input, select, textarea, [role='button'], [role='dialog'], [role='alert'], [role='alertdialog'], [data-testid], [data-test], [data-cy]"));

        try (BufferedWriter w = new BufferedWriter(new FileWriter(f))) {
            w.write("count=" + all.size() + System.lineSeparator());
            w.write("---" + System.lineSeparator());
            int i = 0;
            for (WebElement el : all) {
                StringBuilder sb = new StringBuilder();
                sb.append("[").append(i++).append("] ");
                sb.append(safe(el.getTagName()));

                String txt = oneLine(el.getText());
                if (!txt.isEmpty()) sb.append(" | text=\"").append(trim(txt, 80)).append("\"");

                addAttr(sb, el, "aria-label");
                addAttr(sb, el, "data-testid");
                addAttr(sb, el, "data-test");
                addAttr(sb, el, "data-cy");
                addAttr(sb, el, "role");
                addAttr(sb, el, "name");
                addAttr(sb, el, "id");
                addAttr(sb, el, "placeholder");
                addAttr(sb, el, "type");
                addAttr(sb, el, "href");
                addAttr(sb, el, "value");
                addAttr(sb, el, "class");

                sb.append(System.lineSeparator());
                w.write(sb.toString());
            }
        }
    }

    private static void addAttr(StringBuilder sb, WebElement el, String name) {
        try {
            String v = el.getAttribute(name);
            if (v != null && !v.isBlank()) sb.append(" | ").append(name).append("=\"").append(trim(oneLine(v), 100)).append("\"");
        } catch (Exception ignored) {}
    }
    private static String safe(String s)   { return s == null ? "" : s; }
    private static String oneLine(String s){ return s == null ? "" : s.replaceAll("\\s+", " ").trim(); }
    private static String trim(String s, int n) { return s.length() <= n ? s : s.substring(0, n) + "..."; }
}
