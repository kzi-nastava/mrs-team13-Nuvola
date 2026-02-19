package Nuvola.Projekatsiit2025.dto;

public class CancelRideRequestDTO {
    private String reason; // optional za passenger, required za driver
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
