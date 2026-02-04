package Nuvola.Projekatsiit2025.dto;


import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.Location;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.Route;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ScheduledRideDTO {
    private long id;
    private double price;
    private Location dropoff;
    private Location pickup;
    private LocalDateTime startingTime;
    private String driver;

    public ScheduledRideDTO(Ride ride) {
        this.id = ride.getId();
        this.price = ride.getPrice();
        this.startingTime = ride.getStartTime();
        Driver driver = ride.getDriver();
        this.driver = driver.getFirstName() + " " + driver.getLastName();
        Route route = ride.getRoute();
        this.dropoff = route.getDropoff();
        this.pickup = route.getPickup();
    }
}

