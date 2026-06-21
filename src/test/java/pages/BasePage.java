package pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Shared parent for every page class.
 * Mirrors the course's `base_prod_page` idea: holds the driver, an explicit WebDriverWait,
 * and a few small helpers so individual page classes stay focused on locators + actions.
 */
public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // ---- Generic helpers (used by every page) ----

    protected WebElement waitVisible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    protected WebElement waitClickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    protected void click(By by) {
        WebElement el = waitClickable(by);
        // Scroll into view to avoid toast/overlay interception
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        try {
            Thread.sleep(200); // Brief pause for any animations
            el.click();
        } catch (Exception e) {
            // Fallback to JS click if element is still intercepted
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    protected void click(String Xpath) {
        WebElement el = driver.findElement(By.xpath(Xpath));
        // Scroll into view to avoid toast/overlay interception
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
        try {
            Thread.sleep(200); // Brief pause for any animations
            el.click();
        } catch (Exception e) {
            // Fallback to JS click if element is still intercepted
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    protected void type(By by, String text) {
        WebElement el = waitVisible(by);
        el.clear();
        el.sendKeys(text);
    }

    protected String text(By by) {
        return waitVisible(by).getText();
    }

    protected boolean isPresent(By by) {
        return !driver.findElements(by).isEmpty();
    }

    protected List<WebElement> findAll(By by) {
        return driver.findElements(by);
    }
}
