package dto;

public class CancelRideRequestDTO {

    public String reason;

    public CancelRideRequestDTO() {
    }

    public CancelRideRequestDTO(String reason) {
        this.reason = reason;
    }
}