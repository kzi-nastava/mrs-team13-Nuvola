package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.Rating;
import Nuvola.Projekatsiit2025.model.Report;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.Route;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import Nuvola.Projekatsiit2025.model.enums.VehicleType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class RideHistoryDetailsDTO {
    private Long id;
    private String pickup;
    private String dropoff;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime creationTime;
    private double price;
    private RideStatus status;


    private DriverInfoDTO driver;
    private RouteDTO route;
    private List<ReportInfoDTO> reports = new ArrayList<>();
    private List<RatingInfoDTO> ratings = new ArrayList<>();
    private Long routeId;


    private CreateRideDTO reorderTemplate;

    public RideHistoryDetailsDTO() {}

    public RideHistoryDetailsDTO(Ride ride) {
        this.id = ride.getId();
        this.price = ride.getPrice();
        this.startTime = ride.getStartTime();
        this.endTime = ride.getEndTime();
        this.creationTime = ride.getCreationTime();
        this.status = ride.getStatus();

        Route r = ride.getRoute();
        this.pickup = r != null && r.getPickup() != null ? r.getPickup().toString() : "";
        this.dropoff = r != null && r.getDropoff() != null ? r.getDropoff().toString() : "";

        this.routeId = r != null ? r.getId() : null;

        // route stops for map
        this.route = new RouteDTO();
        if (r != null) {
            if (r.getPickup() != null) {
                this.route.appendStop(new CoordinateDTO(r.getPickup().getLatitude(), r.getPickup().getLongitude(), r.getPickup().getAddress()));
            }
            if (r.getStops() != null) {
                r.getStops().forEach(s -> this.route.appendStop(new CoordinateDTO(s.getLatitude(), s.getLongitude(), s.getAddress())));
            }
            if (r.getDropoff() != null) {
                this.route.appendStop(new CoordinateDTO(r.getDropoff().getLatitude(), r.getDropoff().getLongitude(), r.getDropoff().getAddress()));
            }
        }

        if (ride.getDriver() != null) {
            this.driver = new DriverInfoDTO(ride.getDriver());
        }

        if (ride.getReports() != null) {
            for (Report rep : ride.getReports()) {
                this.reports.add(new ReportInfoDTO(rep));
            }
        }

        if (ride.getRatings() != null) {
            for (Rating rating : ride.getRatings()) {
                this.ratings.add(new RatingInfoDTO(rating));
            }
        }


        CreateRideDTO t = new CreateRideDTO();
        if (r != null && r.getPickup() != null) {
            t.setFrom(new CoordinateDTO(r.getPickup().getLatitude(), r.getPickup().getLongitude(), r.getPickup().getAddress()));
        }
        if (r != null && r.getDropoff() != null) {
            t.setTo(new CoordinateDTO(r.getDropoff().getLatitude(), r.getDropoff().getLongitude(), r.getDropoff().getAddress()));
        }
        if (r != null && r.getStops() != null) {
            List<CoordinateDTO> stops = r.getStops().stream()
                    .map(s -> new CoordinateDTO(s.getLatitude(), s.getLongitude(), s.getAddress()))
                    .toList();
            t.setStops(stops);
        }

        VehicleType type = ride.getDriver() != null && ride.getDriver().getVehicle() != null
                ? ride.getDriver().getVehicle().getType()
                : VehicleType.STANDARD;
        t.setVehicleType(type);
        t.setBabyTransport(false);
        t.setPetTransport(false);
        this.reorderTemplate = t;
    }
}
