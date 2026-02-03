package Nuvola.Projekatsiit2025.dto;

import lombok.Data;

@Data
public class VehiclePositionDTO {
    private Long vehicleId;
    private Double latitude;
    private Double longitude;

    // if the positions is sended as part of a ride, this field contains the ride id
    private Long rideId; // nullable

    public boolean hasRide() {
        return rideId != null;
    }

    public VehiclePositionDTO(Long vehicleId, Double latitude, Double longitude) {
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rideId = null;
    }
}
