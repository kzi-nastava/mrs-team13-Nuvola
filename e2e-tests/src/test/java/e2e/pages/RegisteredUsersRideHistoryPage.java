package e2e.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class RegisteredUsersRideHistoryPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By loadingText = By.xpath("//*[normalize-space()='Loading...']");
    private final By table = By.cssSelector("table");
    private final By nextBtn = By.xpath("//button[normalize-space()='Next']");
    private final By errorDiv = By.cssSelector("div[style*='color:red']");


    public RegisteredUsersRideHistoryPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(table),
                ExpectedConditions.visibilityOfElementLocated(errorDiv),
                ExpectedConditions.visibilityOfElementLocated(loadingText)
        ));

        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.invisibilityOfElementLocated(loadingText));
        } catch (TimeoutException ignored) { }
    }


    private void scrollToElementCompat(WebElement el) {
            Actions actions = new Actions(driver);
            actions.scrollToElement(el).perform();
    }

    public void clickGradeByRideId(long rideId) {
        waitUntilLoaded();
        By gradeButtonInRow = By.xpath(
                "//table//tbody//tr[td[1][normalize-space()='" + rideId + "']]//button[contains(@class,'grade-btn')]"
        );

        WebElement gradeBtn = wait.until(ExpectedConditions.presenceOfElementLocated(gradeButtonInRow));
        scrollToElementCompat(gradeBtn);
        gradeBtn = wait.until(ExpectedConditions.elementToBeClickable(gradeButtonInRow));
        gradeBtn.click();
        wait.until(ExpectedConditions.urlContains("/grading/" + rideId));

    }


    public void clickGradeByRideIdAcrossPages(long rideId, int maxPages) {
        for (int i = 0; i < maxPages; i++) {
            waitUntilLoaded();

            By gradeButtonInRow = By.xpath(
                    "//table//tbody//tr[td[1][normalize-space()='" + rideId + "']]//button[contains(@class,'grade-btn')]"
            );

            if (!driver.findElements(gradeButtonInRow).isEmpty()) {
                clickGradeByRideId(rideId);
                return;
            }

            WebElement next = driver.findElement(nextBtn);
            if (!next.isEnabled()) {
                break;
            }
            next.click();
        }

        throw new NoSuchElementException("Ride id " + rideId + " not found within " + maxPages + " pages.");
    }
}
