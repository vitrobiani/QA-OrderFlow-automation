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
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * NOT a real test — utility to help locator discovery in Session 1.
 *
 * Run via `mvn -q -Dtest=DomDiscovery test` (or main()) and it will:
 *   1. Open each of the four app routes (/, /order, /history, /returns).
 *   2. Wait briefly for React to settle.
 *   3. Dump driver.getPageSource() to discovery/<route>.html
 *   4. Dump a concise "interactive elements" report to discovery/<route>.elements.txt
 *      (every button, link, input, select, role=button, [data-*] node — with text,
 *      aria-label, data-* attributes, and a best-guess CSS selector).
 *
 * Then we (Claude + user) read those files together and lock down the real locators
 * for NavBar / NewOrderPage / OrderHistoryPage / ReturnsPage.
 */
public class DomDiscovery {

    private static final String[] ROUTES = { "/", "/order", "/history", "/returns" };

    public static void main(String[] args) throws Exception {
        File out = new File("discovery");
        if (!out.exists()) out.mkdirs();

        WebDriver driver = base_test_class.initializeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            for (String route : ROUTES) {
                String url = base_test_class.BASE_URL + route;
                System.out.println("=== Discovering " + url + " ===");
                driver.get(url);

                // give React time to mount; explicit wait on <body> is the cheapest signal
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                Thread.sleep(2000);

                String safeName = route.equals("/") ? "home" : route.replaceAll("/", "");
                dumpHtml(driver, new File(out, safeName + ".html"));
                dumpElements(driver, new File(out, safeName + ".elements.txt"));

                System.out.println("  -> discovery/" + safeName + ".html");
                System.out.println("  -> discovery/" + safeName + ".elements.txt");
            }
            System.out.println();
            System.out.println("Discovery complete. Share the discovery/ folder back with Claude.");
        } finally {
            driver.quit();
        }
    }

    private static void dumpHtml(WebDriver driver, File f) throws Exception {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(f))) {
            w.write(driver.getPageSource());
        }
    }

    /**
     * Walks every potentially-clickable element and writes one line per element:
     *   <tag> | text="..." | aria-label="..." | data-*="..." | role="..." | css="..."
     */
    private static void dumpElements(WebDriver driver, File f) throws Exception {
        List<WebElement> all = driver.findElements(By.cssSelector(
                "button, a, input, select, textarea, [role='button'], [data-testid], [data-test], [data-cy]"));

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
    private static String trim(String s, int n) { return s.length() <= n ? s : s.substring(0, n) + "…"; }
}
