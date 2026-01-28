package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.Ride;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class TrackingRideDTO {
    private Long id;
    private RouteDTO  route;
    private Long driverId;
    private double price;
    private String dropoff;
    private String pickup;
    private LocalDateTime startingTime;
    private boolean isFavouriteRoute;

    public TrackingRideDTO(Long id, RouteDTO route, Long driverId, double price, String dropoff, String pickup, LocalDateTime startingTime, boolean isFavouriteRoute) {
        this.id = id;
        this.route = route;
        this.driverId = driverId;
        this.price = price;
        this.dropoff = dropoff;
        this.pickup = pickup;
        this.startingTime = startingTime;
        this.isFavouriteRoute = isFavouriteRoute;
    }


}
