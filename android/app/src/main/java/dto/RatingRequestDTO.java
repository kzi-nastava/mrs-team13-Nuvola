package dto;

public class RatingRequestDTO {
    public int vehicleRating;
    public int driverRating;
    public String comment;
    public long rideId;
    public String username;

    public RatingRequestDTO(int vehicleRating, int driverRating, String comment, long rideId, String username) {
        this.vehicleRating = vehicleRating;
        this.driverRating = driverRating;
        this.comment = comment;
        this.rideId = rideId;
        this.username = username;
    }
}