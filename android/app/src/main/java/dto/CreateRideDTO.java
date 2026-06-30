package dto;

import java.util.ArrayList;
import java.util.List;

public class CreateRideDTO {

    private CoordinateDTO from;
    private CoordinateDTO to;

    private List<CoordinateDTO> stops =
            new ArrayList<>();

    private List<String> passengerEmails =
            new ArrayList<>();

    private String vehicleType;

    private boolean babyTransport;
    private boolean petTransport;

    private String scheduledTime;

    private double distanceKm;

    public CreateRideDTO() {
    }

    public CoordinateDTO getFrom() {
        return from;
    }

    public void setFrom(CoordinateDTO from) {
        this.from = from;
    }

    public CoordinateDTO getTo() {
        return to;
    }

    public void setTo(CoordinateDTO to) {
        this.to = to;
    }

    public List<CoordinateDTO> getStops() {
        return stops;
    }

    public void setStops(List<CoordinateDTO> stops) {
        this.stops = stops;
    }

    public List<String> getPassengerEmails() {
        return passengerEmails;
    }

    public void setPassengerEmails(
            List<String> passengerEmails
    ) {
        this.passengerEmails = passengerEmails;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public boolean isBabyTransport() {
        return babyTransport;
    }

    public void setBabyTransport(
            boolean babyTransport
    ) {
        this.babyTransport = babyTransport;
    }

    public boolean isPetTransport() {
        return petTransport;
    }

    public void setPetTransport(
            boolean petTransport
    ) {
        this.petTransport = petTransport;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(
            String scheduledTime
    ) {
        this.scheduledTime = scheduledTime;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(
            double distanceKm
    ) {
        this.distanceKm = distanceKm;
    }
}