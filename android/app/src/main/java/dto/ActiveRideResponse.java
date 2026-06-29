package dto;

public class ActiveRideResponse {

    private boolean hasActiveRide;
    private Long rideId;
    private String status;

    public ActiveRideResponse() {
    }

    public boolean isHasActiveRide() {
        return hasActiveRide;
    }

    public void setHasActiveRide(boolean hasActiveRide) {
        this.hasActiveRide = hasActiveRide;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}