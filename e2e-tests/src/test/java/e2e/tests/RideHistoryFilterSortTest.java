package e2e.tests;

import e2e.base.BaseTest;
import e2e.pages.HomePage;
import e2e.pages.LoginPage;
import e2e.pages.MainPage;
import e2e.pages.RegisteredUsersRideHistoryPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RideHistoryFilterSortTest extends BaseTest {

    private static final String EMAIL = "student3.e2e@gmail.com";
    private static final String PASSWORD = "test12345";

    @Test
    @DisplayName("Happy path - filter history by broad date range and sort by price ascending")
    public void filterAndSortRideHistory_successfully() {
        RegisteredUsersRideHistoryPage rideHistoryPage = openRideHistory();

        rideHistoryPage.applyDateFilter("2020-01-01", "2035-12-31");
        rideHistoryPage.sortBy("price", "asc");

        List<Double> prices = rideHistoryPage.getPrices();
        assertTrue(prices.size() > 0, "Expected at least one ride in the broad date range");
        assertTrue(rideHistoryPage.isSortedAscending(prices), "Expected prices to be sorted ascending");
    }

    @Test
    @DisplayName("Exception path - future date filter shows no history rows")
    public void filterRideHistory_noResults() {
        RegisteredUsersRideHistoryPage rideHistoryPage = openRideHistory();

        rideHistoryPage.applyDateFilter("2099-01-01", "2099-12-31");

        assertEquals(0, rideHistoryPage.getRowCount(), "Expected no rides for far future date range");
    }

    private RegisteredUsersRideHistoryPage openRideHistory() {
        MainPage mainPage = new MainPage(driver);
        mainPage.open();

        LoginPage loginPage = mainPage.clickLogIn();
        loginPage.login(EMAIL, PASSWORD);

        HomePage homePage = new HomePage(driver);
        homePage.clickRideHistory();

        RegisteredUsersRideHistoryPage rideHistoryPage = new RegisteredUsersRideHistoryPage(driver);
        rideHistoryPage.waitUntilLoaded();
        return rideHistoryPage;
    }
}
