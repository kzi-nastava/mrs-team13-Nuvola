package Nuvola.Projekatsiit2025.dto;

import lombok.Data;

@Data
public class VehiclePositionDTO {
    private Long vehicleId;
    private Double latitude;
    private Double longitude;

    public VehiclePositionDTO(Long vehicleId, Double latitude, Double longitude) {
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;;
    }
}
