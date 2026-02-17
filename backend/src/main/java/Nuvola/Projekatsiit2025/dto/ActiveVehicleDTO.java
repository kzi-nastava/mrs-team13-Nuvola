package Nuvola.Projekatsiit2025.dto;

import lombok.Data;
import lombok.NoArgsConstructor;


// Delete this class later
@Data
@NoArgsConstructor
public class ActiveVehicleDTO {
    private Double latitude;
    private Double longitude;
    private boolean occupied;
    private Long vehicleId;

    public ActiveVehicleDTO(Double latitude, Double longitude, boolean occupied, Long vehicleId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.occupied = occupied;
        this.vehicleId = vehicleId;
    }

}
