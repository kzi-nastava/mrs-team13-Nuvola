package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.CreateDriverDTO;
import Nuvola.Projekatsiit2025.model.Driver;

public interface DriverService {

    Driver createDriver(CreateDriverDTO dto);
    void logoutDriver(Long driverId);

}
