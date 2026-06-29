package dto;

import java.util.ArrayList;
import java.util.List;

public class DriverAssignedRideDTO {

    private Long id;

    private String pickup;
    private String dropoff;
    private String status;
    private String scheduledTime;

    private Double price;

    private List<String> stops =
            new ArrayList<>();

    private List<String> passengers =
            new ArrayList<>();

    public DriverAssignedRideDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(
            String scheduledTime
    ) {
        this.scheduledTime = scheduledTime;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<String> getStops() {
        return stops;
    }

    public void setStops(List<String> stops) {
        this.stops =
                stops == null
                        ? new ArrayList<>()
                        : stops;
    }

    public List<String> getPassengers() {
        return passengers;
    }

    public void setPassengers(
            List<String> passengers
    ) {
        this.passengers =
                passengers == null
                        ? new ArrayList<>()
                        : passengers;
    }
}