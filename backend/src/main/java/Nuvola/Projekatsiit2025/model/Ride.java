package Nuvola.Projekatsiit2025.model;

import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class Ride {
    private Long id;
    private double price;
    private RideStatus status;
    private Route route;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime creationTime;
    private boolean isPanic;
    private List<RegisteredUser> otherPassengers;
    private Driver driver;
    private RegisteredUser creator;
    private List<Report> reports;
    private Set<Rating> ratings;

    public Ride() {
        this.isPanic = false; // default
    }

    public Ride(Long id, double price, RideStatus status, Route route, LocalDateTime startTime, LocalDateTime endTime,
                LocalDateTime creationTime, boolean isPanic) {
        this.id = id;
        this.price = price;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.creationTime = creationTime;
        this.isPanic = isPanic;
    }


}
