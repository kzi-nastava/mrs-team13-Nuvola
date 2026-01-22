package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.Location;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.Route;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DriverRideHistoryItemDTO {
    private long id;
    private double price;
    private Location dropoff;
    private Location pickup;
    private LocalDateTime startingTime;
    private String driver;
    private boolean isFavouriteRoute;

    public DriverRideHistoryItemDTO() {}

    public DriverRideHistoryItemDTO(Ride ride) {
        id = ride.getId();
        price = ride.getPrice();
        Route route = ride.getRoute();
        dropoff = route.getDropoff();
        pickup = route.getPickup();
        startingTime = ride.getStartTime();
        Driver rideDriver = ride.getDriver();
        driver = rideDriver.getFirstName() + " " + rideDriver.getLastName();
        isFavouriteRoute = route.isFavourite();

    }

}
