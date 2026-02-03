package Nuvola.Projekatsiit2025.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateReviewDTO {
    private int driverRating;
    private int vehicleRating;
    private String comment;
    private Long rideId;
    private String username;


}
