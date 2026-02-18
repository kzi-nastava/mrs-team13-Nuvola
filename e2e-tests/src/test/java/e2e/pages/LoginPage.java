package e2e.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;

public class LoginPage {
    private WebDriver driver;
    private WebDriverWait wait;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void login(String email, String password) {
        WebElement emailInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']"))
        );
        emailInput.sendKeys(email);
        driver.findElement(By.cssSelector("input[formcontrolname='password']")).sendKeys(password);
        driver.findElement(By.cssSelector("button.primary-btn")).click();

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(4));
            WebElement okBtn = shortWait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[normalize-space()='OK' or normalize-space()='Ok']")
                    )
            );
            okBtn.click();
        } catch (Exception e) {
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[contains(@class,'nav-link') and normalize-space()='Log out']")
        ));
    }
}