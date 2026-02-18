package e2e.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.List;

public class FavoriteRoutesModal {
    private WebDriver driver;
    private WebDriverWait wait;

    public FavoriteRoutesModal(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void waitForModal() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.fav-modal")
        ));
    }

    public void selectFirstRoute() {
        waitForModal();
        WebElement orderAgainBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button.fav-order"))
        );
        orderAgainBtn.click();
    }

    public boolean isNoFavoritesMessageVisible() {
        waitForModal();
        List<WebElement> emptyMsg = driver.findElements(
                By.xpath("//div[contains(@class,'fav-empty') and contains(text(),'don')]")
        );
        return !emptyMsg.isEmpty();
    }

    public boolean isModalVisible() {
        List<WebElement> modal = driver.findElements(By.cssSelector("div.fav-modal"));
        return !modal.isEmpty();
    }
}