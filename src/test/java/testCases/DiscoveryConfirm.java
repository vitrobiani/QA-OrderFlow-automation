package testCases;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DiscoveryConfirm {
    public static void main(String[] args) throws Exception {
        File out = new File("discovery");
        if (!out.exists()) out.mkdirs();

        WebDriver driver = base_test_class.initializeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        try {
            driver.get(base_test_class.BASE_URL + "/order");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("category-select")));
            Thread.sleep(800);
            new Select(driver.findElement(By.id("category-select"))).selectByValue("laptops");
            Thread.sleep(1500);

            WebElement add = driver.findElement(By.cssSelector("[data-testid^='select-product-']"));
            add.click();
            Thread.sleep(800);

            driver.findElement(By.id("btn-submit-order")).click();
            Thread.sleep(1500);

            try (BufferedWriter w = new BufferedWriter(new FileWriter(new File(out, "confirm-dialog.html")))) {
                w.write(driver.getPageSource());
            }
            System.out.println("dumped discovery/confirm-dialog.html");
        } finally {
            driver.quit();
        }
    }
}
