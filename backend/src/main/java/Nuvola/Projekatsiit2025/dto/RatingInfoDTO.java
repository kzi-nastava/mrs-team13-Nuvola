package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.Rating;
import lombok.Data;

@Data
public class RatingInfoDTO {
    private Long id;
    private Integer vehicleRating;
    private Integer driverRating;
    private String comment;
    private String authorUsername;

    public RatingInfoDTO() {}

    public RatingInfoDTO(Rating r) {
        this.id = r.getId();
        this.vehicleRating = r.getVehicleRating();
        this.driverRating = r.getDriverRating();
        this.comment = r.getComment();
        this.authorUsername = r.getAuthor() != null ? r.getAuthor().getUsername() : null;
    }
}
