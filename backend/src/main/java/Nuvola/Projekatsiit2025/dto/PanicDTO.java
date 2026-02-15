package Nuvola.Projekatsiit2025.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PanicDTO {
    private Long rideId;
    private Long driverId;
    private Long passengerId;
}
