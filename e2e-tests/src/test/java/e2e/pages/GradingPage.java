package e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class GradingPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title = By.cssSelector("h2.title");
    private final By vehicleSelect = By.id("vehicleRating");
    private final By driverSelect  = By.id("driverRating");
    private final By commentTextArea = By.id("comment");
    private final By submitBtn = By.cssSelector("button.btn.primary[type='submit']");

    private final By successMsg = By.cssSelector("p.msg.ok");
    private final By errorMsg = By.cssSelector("p.msg.error");

    public GradingPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void waitUntilOpened() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(title));
        wait.until(ExpectedConditions.visibilityOfElementLocated(vehicleSelect));
        wait.until(ExpectedConditions.visibilityOfElementLocated(driverSelect));
    }

    public void setVehicleRating(int rating) {
        waitUntilOpened();
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Vehicle rating must be 1-5");

        WebElement sel = wait.until(ExpectedConditions.elementToBeClickable(vehicleSelect));
        new Select(sel).selectByVisibleText(String.valueOf(rating));
    }

    public void setDriverRating(int rating) {
        waitUntilOpened();
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Driver rating must be 1-5");

        WebElement sel = wait.until(ExpectedConditions.elementToBeClickable(driverSelect));
        new Select(sel).selectByVisibleText(String.valueOf(rating));
    }

    public void setComment(String comment) {
        WebElement ta = wait.until(ExpectedConditions.visibilityOfElementLocated(commentTextArea));
        ta.clear();
        if (comment != null) ta.sendKeys(comment);
    }

    public void submit() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitBtn));
        btn.click();
    }


    public void submitRating(int vehicleRating, int driverRating, String comment) {
        setVehicleRating(vehicleRating);
        setDriverRating(driverRating);
        setComment(comment);
        submit();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(successMsg),
                ExpectedConditions.visibilityOfElementLocated(errorMsg)
        ));
    }

    public void submitRatingWithoutAGradeField(int vehicleRating) {
        setVehicleRating(vehicleRating);
        submit();

        WebElement err = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMsg));
    }

    public String getSuccessMessage() {
        return driver.findElements(successMsg).isEmpty()
                ? null
                : driver.findElement(successMsg).getText().trim();
    }

    public String getErrorMessage() {
        return driver.findElements(errorMsg).isEmpty()
                ? null
                : driver.findElement(errorMsg).getText().trim();
    }

    public boolean isSubmitDisabled() {
        return !driver.findElement(submitBtn).isEnabled();
    }

}
