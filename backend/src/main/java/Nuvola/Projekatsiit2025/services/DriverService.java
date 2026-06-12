package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.CreateDriverDTO;
import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.enums.DriverStatus;


public interface DriverService {

    Driver createDriver(CreateDriverDTO dto);
    void logoutDriver(Long driverId);
    void loginDriver(Long driverId);
    DriverStatus changeStatus(Long driverId, DriverStatus status);

}
