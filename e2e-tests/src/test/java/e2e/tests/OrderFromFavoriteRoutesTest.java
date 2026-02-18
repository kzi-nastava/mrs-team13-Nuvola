package e2e.tests;

import e2e.base.BaseTest;
import e2e.pages.*;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class OrderFromFavoriteRoutesTest extends BaseTest {

    @Test
    @DisplayName("Successfully order ride from favorite routes")
    void orderFromFavoriteRoutes_success() throws InterruptedException {

        MainPage mainPage = new MainPage(driver);
        mainPage.open();
        Thread.sleep(1000);

        // login
        LoginPage loginPage = mainPage.clickLogIn();
        Thread.sleep(1000);

        loginPage.login("nikola@gmail.com", "nikola12345");
        Thread.sleep(1000);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("logedin-home"));

        // open panel
        HomePage homePage = new HomePage(driver);
        homePage.clickOrderARide();
        Thread.sleep(1000);

        // open favorite routes
        FavoriteRoutesModal modal = homePage.clickOrderFromFavoriteRoutes();
        Thread.sleep(1000);

        assertTrue(modal.isModalVisible(), "Modal should be visible");
        Thread.sleep(1000);

        // choose first route
        modal.selectFirstRoute();
        Thread.sleep(1000);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                org.openqa.selenium.By.cssSelector("div.fav-modal")
        ));
        Thread.sleep(1000);

        // order ride
        homePage.clickOrderRide();
        Thread.sleep(1000);

        // check  notification
        WebElement toast = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("div.toast.rideapproved")
                )
        );
        assertTrue(toast.isDisplayed(), "Ride Approved toast should be visible");
        assertTrue(toast.getText().contains("Ride Approved"), "Toast should contain 'Ride Approved'");

    }

    @Test
    @DisplayName("No favorite routes case")
    void orderFromFavoriteRoutes_noFavorites() throws InterruptedException {
        MainPage mainPage = new MainPage(driver);
        mainPage.open();
        Thread.sleep(1000);

        // login
        LoginPage loginPage = mainPage.clickLogIn();
        Thread.sleep(1000);
        loginPage.login("milica@gmail.com", "milica123");
        Thread.sleep(1000);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("logedin-home"));
        Thread.sleep(1000);

        // open panel
        HomePage homePage = new HomePage(driver);
        homePage.clickOrderARide();
        Thread.sleep(1000);

        //open favorite routes
        FavoriteRoutesModal modal = homePage.clickOrderFromFavoriteRoutes();
        Thread.sleep(1000);
        assertTrue(modal.isModalVisible());
        assertTrue(modal.isNoFavoritesMessageVisible(),
                "Should show empty favorites message");
    }
}