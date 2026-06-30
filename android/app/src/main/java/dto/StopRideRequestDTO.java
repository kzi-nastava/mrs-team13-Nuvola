package dto;

public class StopRideRequestDTO {

    public double lat;
    public double lng;
    public String stoppedAt;
    public String address;

    public StopRideRequestDTO() {
    }

    public StopRideRequestDTO(
            double lat,
            double lng,
            String stoppedAt,
            String address
    ) {
        this.lat = lat;
        this.lng = lng;
        this.stoppedAt = stoppedAt;
        this.address = address;
    }
}