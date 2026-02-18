package e2e.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;

public class HomePage {
    private WebDriver driver;
    private WebDriverWait wait;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void clickOrderARide() {
        WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button.order-btn"))
        );
        btn.click();
    }

    public FavoriteRoutesModal clickOrderFromFavoriteRoutes() {
        WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-fav"))
        );
        btn.click();
        return new FavoriteRoutesModal(driver);
    }

    public void clickOrderRide() {
        WebElement btn = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//button[contains(@class,'btn-primary') and normalize-space()='Order ride']")
                )
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
        wait.until(ExpectedConditions.elementToBeClickable(btn));
        btn.click();
    }
}