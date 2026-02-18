package Nuvola.Projekatsiit2025.dto;

import java.time.LocalDateTime;

public class StopRideRequestDTO {
    private double lat;
    private double lng;
    private LocalDateTime stoppedAt;
    private String address;

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public LocalDateTime getStoppedAt() { return stoppedAt; }
    public void setStoppedAt(LocalDateTime stoppedAt) { this.stoppedAt = stoppedAt; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
