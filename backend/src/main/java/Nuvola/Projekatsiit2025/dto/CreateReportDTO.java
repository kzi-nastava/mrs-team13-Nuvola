package Nuvola.Projekatsiit2025.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateReportDTO {
    private String reason;
    private String authorUsername;
    private Long rideId;
}
