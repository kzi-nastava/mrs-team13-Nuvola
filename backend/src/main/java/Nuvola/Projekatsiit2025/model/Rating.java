package Nuvola.Projekatsiit2025.model;

import lombok.Data;

@Data
public class Rating {
    private Long id;
    private Integer vehicleRating;
    private Integer driverRating;
    private String comment;
    private Ride ride;  // maybe it's not necessary for this to be bidirectional?
    private RegisteredUser author;

    public Rating(Integer vehicleRating, Integer driverRating, String comment) {}
}
