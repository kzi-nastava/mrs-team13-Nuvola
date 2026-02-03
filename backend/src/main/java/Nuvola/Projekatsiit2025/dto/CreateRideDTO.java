package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.enums.VehicleType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CreateRideDTO {

    @NotNull
    private CoordinateDTO from;

    @NotNull
    private CoordinateDTO to;

    private List<CoordinateDTO> stops = new ArrayList<>();

    private List<String> passengerEmails = new ArrayList<>();

    @NotNull
    private VehicleType vehicleType;

    private boolean babyTransport;
    private boolean petTransport;

    private LocalDateTime scheduledTime;

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

    public void setPassengerEmails(List<String> passengerEmails) {
        this.passengerEmails = passengerEmails;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public boolean isBabyTransport() {
        return babyTransport;
    }

    public void setBabyTransport(boolean babyTransport) {
        this.babyTransport = babyTransport;
    }

    public boolean isPetTransport() {
        return petTransport;
    }

    public void setPetTransport(boolean petTransport) {
        this.petTransport = petTransport;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
}
