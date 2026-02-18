package e2e.pages;

import e2e.exceptions.LoadsTooLongException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.List;

public class HomePage {
    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(css = "button.nav-link.ride-history")
    private List<WebElement> rideHistoryButtons;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));

    }

    public void clickRideHistory() {
        WebElement btn = wait.until(d -> rideHistoryButtons.stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Ride history button not visible")));

        wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
        wait.until(ExpectedConditions.urlContains("/ride-history"));

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