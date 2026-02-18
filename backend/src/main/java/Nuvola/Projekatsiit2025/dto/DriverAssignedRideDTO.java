package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.Location;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DriverAssignedRideDTO {
    private Long id;
    private RideStatus status;
    private double price;
    private LocalDateTime scheduledTime;

    private String pickup;
    private String dropoff;
    private List<String> stops;
    private List<String> passengers;

    public DriverAssignedRideDTO(Ride ride) {
        this.id = ride.getId();
        this.status = ride.getStatus();
        this.price = ride.getPrice();
        this.scheduledTime = ride.getStartTime();

        this.pickup = ride.getRoute().getPickup().getAddress();
        this.dropoff = ride.getRoute().getDropoff().getAddress();

        this.stops = ride.getRoute().getStops() != null
                ? ride.getRoute().getStops().stream()
                .map(Location::getAddress)
                .collect(Collectors.toList())
                : List.of();

        List<String> passengerEmails = new ArrayList<>();

        passengerEmails.add(ride.getCreator().getEmail());

        ride.getOtherPassengers()
                .forEach(u -> passengerEmails.add(u.getEmail()));

        this.passengers = passengerEmails;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getPickup() {
        return pickup;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public String getDropoff() {
        return dropoff;
    }

    public void setDropoff(String dropoff) {
        this.dropoff = dropoff;
    }

    public List<String> getStops() {
        return stops;
    }

    public void setStops(List<String> stops) {
        this.stops = stops;
    }

    public List<String> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<String> passengers) {
        this.passengers = passengers;
    }
}
