package Nuvola.Projekatsiit2025.dto;

import lombok.Data;

@Data
public class PositionDTO {
    private Double latitude;
    private Double longitude;
    public PositionDTO(Double latitude, Double longitude) {}
}
