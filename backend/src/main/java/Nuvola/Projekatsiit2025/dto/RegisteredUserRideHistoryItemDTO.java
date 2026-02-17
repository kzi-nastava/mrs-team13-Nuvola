package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.Route;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class RegisteredUserRideHistoryItemDTO {
    private Long id;
    private String pickup;
    private String dropoff;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime creationTime;
    private Double price;
    private String status;
    private boolean favourite;
    // route for reorder
    private Long routeId;
    private DriverInfoDTO driver;
    public RegisteredUserRideHistoryItemDTO(Ride ride) {
        this.id = ride.getId();
        //this.pickup = ride.getRoute().getPickup().toString();
        //this.dropoff = ride.getRoute().getDropoff().toString();

        Route r = ride.getRoute();

        this.pickup = (r != null && r.getPickup() != null)
                ? safeAddress(r.getPickup().getAddress(),
                r.getPickup().getLatitude(),
                r.getPickup().getLongitude())
                : "";

        this.dropoff = (r != null && r.getDropoff() != null)
                ? safeAddress(r.getDropoff().getAddress(),
                r.getDropoff().getLatitude(),
                r.getDropoff().getLongitude())
                : "";
        this.startTime = ride.getStartTime();
        this.endTime = ride.getEndTime();
        this.creationTime = ride.getCreationTime();
        this.price = ride.getPrice();
        this.status = ride.getStatus().name();
        this.favourite = ride.getRoute() != null && ride.getRoute().isFavourite();
        if (ride.getDriver() != null) {
            this.driver = new DriverInfoDTO(ride.getDriver());        }

        if (r != null) {
            this.routeId = r.getId();
        }
    }
    private String safeAddress(String address, Double lat, Double lon) {
        if (address != null && !address.isBlank()) return address;
        if (lat != null && lon != null) return lat + " , " + lon;
        return "";
    }


}
