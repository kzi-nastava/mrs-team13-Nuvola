package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.enums.DriverStatus;
import Nuvola.Projekatsiit2025.model.enums.VehicleType;
import lombok.Data;

@Data
public class DriverInfoDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private DriverStatus status;
    private VehicleType vehicleType;

    public DriverInfoDTO() {}

    public DriverInfoDTO(Driver d) {
        this.id = d.getId();
        this.firstName = d.getFirstName();
        this.lastName = d.getLastName();
        this.phone = d.getPhone();
        this.email = d.getEmail();
        this.status = d.getStatus();
        this.vehicleType = d.getVehicle() != null ? d.getVehicle().getType() : null;
    }
}
