package pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
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
        waitClickable(by).click();
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
