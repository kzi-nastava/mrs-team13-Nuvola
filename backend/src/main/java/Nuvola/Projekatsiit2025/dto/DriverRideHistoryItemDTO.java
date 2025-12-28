package Nuvola.Projekatsiit2025.dto;

import java.time.LocalDateTime;

public class DriverRideHistoryItemDTO {
    private long id;
    private double price;
    private String dropoff;
    private String pickup;
    private LocalDateTime startingTime;
    private String driver;
    private boolean isFavouriteRoute;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDropoff() {
        return dropoff;
    }

    public void setDropoff(String dropoff) {
        this.dropoff = dropoff;
    }

    public String getPickup() {
        return pickup;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public LocalDateTime getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(LocalDateTime startingTime) {
        this.startingTime = startingTime;
    }


    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public boolean isFavouriteRoute() {
        return isFavouriteRoute;
    }

    public void setFavouriteRoute(boolean favouriteRoute) {
        isFavouriteRoute = favouriteRoute;
    }
}
