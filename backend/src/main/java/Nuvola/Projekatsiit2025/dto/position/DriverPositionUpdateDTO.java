package Nuvola.Projekatsiit2025.dto.position;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DriverPositionUpdateDTO {
    Double latitude;
    Double longitude;
    Long driverId;
    boolean occupied;
    boolean toRemove;

    public DriverPositionUpdateDTO(Double latitude, Double longitude, Long driverId, boolean occupied, boolean toRemove) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.driverId = driverId;
        this.occupied = occupied;
        this.toRemove = toRemove;
    }
}
