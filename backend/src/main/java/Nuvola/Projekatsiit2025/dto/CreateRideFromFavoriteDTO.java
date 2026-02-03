package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.enums.VehicleType;
import java.util.List;

public class CreateRideFromFavoriteDTO {

    private List<String> passengerEmails;
    private VehicleType vehicleType;
    private boolean babyTransport;
    private boolean petTransport;
    private String scheduledTime;

    public CreateRideFromFavoriteDTO() {}

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

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
}
