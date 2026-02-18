package e2e.tests;

import e2e.base.BaseTest;
import e2e.pages.*;
import org.junit.jupiter.api.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GradeTest extends BaseTest {

    @Test
    @Order(1)
    @DisplayName("Happy path - Successfully make a grade for a driver/vehicle")
    public void grade_successfully() throws InterruptedException
    {
        String email = "vukdj12@gmail.com";
        String password = "123456789";
        long rideId = 3L;

        MainPage mainPage = new MainPage(driver);
        mainPage.open();

        LoginPage loginPage = mainPage.clickLogIn();
        loginPage.login(email, password);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        HomePage homePage = new HomePage(driver);
        homePage.clickRideHistory();

        RegisteredUsersRideHistoryPage rideHistoryPage = new RegisteredUsersRideHistoryPage(driver);


        rideHistoryPage.clickGradeByRideIdAcrossPages(rideId, 3);

        wait.until(ExpectedConditions.urlContains("/grading/" + rideId));

        GradingPage gradingPage = new GradingPage(driver);
        gradingPage.waitUntilOpened();

        gradingPage.submitRating(5, 5, "Great ride!");

        String ok = gradingPage.getSuccessMessage();
        String err = gradingPage.getErrorMessage();

        assertNotNull(ok, "Expected success message, got error: " + err);

    }

    @Test
    @Order(2)
    @DisplayName("Try to grade a ride that has already been graded")
    public void grade_alreadyGraded() throws InterruptedException
    {
        String email = "vukdj12@gmail.com";
        String password = "123456789";
        long rideId = 3L;  // Already graded in previous test

        MainPage mainPage = new MainPage(driver);
        mainPage.open();

        LoginPage loginPage = mainPage.clickLogIn();
        loginPage.login(email, password);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        HomePage homePage = new HomePage(driver);
        homePage.clickRideHistory();

        RegisteredUsersRideHistoryPage rideHistoryPage = new RegisteredUsersRideHistoryPage(driver);


        rideHistoryPage.clickGradeByRideIdAcrossPages(rideId, 3);

        wait.until(ExpectedConditions.urlContains("/grading/" + rideId));

        GradingPage gradingPage = new GradingPage(driver);
        gradingPage.waitUntilOpened();

        gradingPage.submitRating(5, 5, "Great ride!");

        String ok = gradingPage.getSuccessMessage();
        String err = gradingPage.getErrorMessage();

        assertNotNull(err, "Expected error message, got success: " + ok);

    }


    @Test
    @Order(3)
    @DisplayName("Try to to leave a grade field empty and submit")
    public void grade() throws InterruptedException
    {
        String email = "vukdj12@gmail.com";
        String password = "123456789";
        long rideId = 3L;

        MainPage mainPage = new MainPage(driver);
        mainPage.open();

        LoginPage loginPage = mainPage.clickLogIn();
        loginPage.login(email, password);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        HomePage homePage = new HomePage(driver);
        homePage.clickRideHistory();

        RegisteredUsersRideHistoryPage rideHistoryPage = new RegisteredUsersRideHistoryPage(driver);


        rideHistoryPage.clickGradeByRideIdAcrossPages(rideId, 3);

        wait.until(ExpectedConditions.urlContains("/grading/" + rideId));

        GradingPage gradingPage = new GradingPage(driver);
        gradingPage.waitUntilOpened();

        gradingPage.submitRatingWithoutAGradeField(3);

        String ok = gradingPage.getSuccessMessage();
        String err = gradingPage.getErrorMessage();

        assertNotNull(err, "Expected error message, got success: " + ok);

    }



}
