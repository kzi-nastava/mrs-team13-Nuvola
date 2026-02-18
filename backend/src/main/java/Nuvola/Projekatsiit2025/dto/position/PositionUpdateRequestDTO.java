package Nuvola.Projekatsiit2025.dto.position;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PositionUpdateRequestDTO {
    Long driverId;
    Double latitude;
    Double longitude;
}
