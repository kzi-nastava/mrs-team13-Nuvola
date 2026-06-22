package e2e.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class RegisteredUsersRideHistoryPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By loadingText = By.xpath("//*[normalize-space()='Loading...']");
    private final By table = By.cssSelector("table");
    private final By nextBtn = By.xpath("//button[normalize-space()='Next']");
    private final By errorDiv = By.cssSelector("div[style*='color:red']");
    private final By fromDateInput = By.xpath("//label[normalize-space()='From']/following::input[@type='date'][1]");
    private final By toDateInput = By.xpath("//label[normalize-space()='To']/following::input[@type='date'][1]");
    private final By sortBySelect = By.xpath("//label[normalize-space()='Sort by']/following::select[1]");
    private final By sortOrderSelect = By.xpath("//label[normalize-space()='Order']/following::select[1]");
    private final By applyButton = By.xpath("//button[normalize-space()='Apply']");
    private final By tableRows = By.cssSelector("table tbody tr");


    public RegisteredUsersRideHistoryPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void waitUntilLoaded() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(fromDateInput),
                    ExpectedConditions.visibilityOfElementLocated(table),
                    ExpectedConditions.visibilityOfElementLocated(errorDiv),
                    ExpectedConditions.visibilityOfElementLocated(loadingText)
            ));
        } catch (TimeoutException e) {
            String bodyText = "";
            try {
                bodyText = driver.findElement(By.tagName("body")).getText();
            } catch (Exception ignored) {
            }
            throw new TimeoutException(
                    "Ride history page did not render expected controls. Current URL: "
                            + driver.getCurrentUrl()
                            + ". Page title: "
                            + driver.getTitle()
                            + ". Body text: "
                            + bodyText,
                    e
            );
        }

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

    public void applyDateFilter(String fromDate, String toDate) {
        setInputValue(fromDateInput, fromDate);
        setInputValue(toDateInput, toDate);
        clickApplyAndWait();
    }

    public void sortBy(String sortBy, String sortOrder) {
        new Select(wait.until(ExpectedConditions.elementToBeClickable(sortBySelect))).selectByValue(sortBy);
        new Select(wait.until(ExpectedConditions.elementToBeClickable(sortOrderSelect))).selectByValue(sortOrder);
        clickApplyAndWait();
    }

    public int getRowCount() {
        waitUntilLoaded();
        return driver.findElements(tableRows).size();
    }

    public List<Double> getPrices() {
        waitUntilLoaded();
        List<Double> prices = new ArrayList<>();
        for (WebElement row : driver.findElements(tableRows)) {
            String priceText = row.findElements(By.cssSelector("td")).get(5).getText().trim();
            prices.add(Double.parseDouble(priceText));
        }
        return prices;
    }

    public boolean isSortedAscending(List<Double> values) {
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i - 1) > values.get(i)) {
                return false;
            }
        }
        return true;
    }

    private void clickApplyAndWait() {
        wait.until(ExpectedConditions.elementToBeClickable(applyButton)).click();
        waitUntilLoaded();
    }

    private void setInputValue(By locator, String value) {
        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input', { bubbles: true }));",
                input,
                value
        );
    }
}
