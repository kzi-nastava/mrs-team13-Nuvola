package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.Ride;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TrackingRideDTO {
    private Long id;
    private RouteDTO  route;
    private Duration estimatedDuration;
    private Long driverId;
    private double price;
    private String dropoff;
    private String pickup;
    private LocalDateTime startingTime;
    private boolean isFavouriteRoute;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RouteDTO getRoute() {
        return route;
    }

    public void setRoute(RouteDTO route) {
        this.route = route;
    }

    public Duration getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Duration estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
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

    public boolean isFavouriteRoute() {
        return isFavouriteRoute;
    }

    public void setFavouriteRoute(boolean favouriteRoute) {
        isFavouriteRoute = favouriteRoute;
    }
}
