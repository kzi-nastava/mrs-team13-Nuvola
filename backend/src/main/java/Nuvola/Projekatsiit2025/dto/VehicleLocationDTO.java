package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.Location;
import lombok.Data;


// delete later with vehicle tracking service
@Data
public class VehicleLocationDTO {
    private Double latitude;
    private Double longitude;
    private Long vehicleId;
    private String status;
    private String regNumber;

    public VehicleLocationDTO() {}

    public VehicleLocationDTO(Driver driver) {
        this.longitude = driver.getVehicle().getLongitude();
        this.latitude = driver.getVehicle().getLatitude();
        this.status = driver.getStatus().toString();
        this.regNumber = driver.getVehicle().getRegNumber();

    }

    public VehicleLocationDTO(VehiclePositionDTO position, String status, String regNumber) {
        this.latitude = position.getLatitude();
        this.longitude = position.getLongitude();
        this.vehicleId = position.getVehicleId();
        this.status = status;
        this.regNumber = regNumber;
    }
}
