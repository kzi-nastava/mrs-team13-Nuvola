package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.VehicleLocationDTO;
import Nuvola.Projekatsiit2025.dto.VehiclePositionDTO;

import java.util.List;

public interface VehicleTrackingService {
    public List<VehicleLocationDTO> getAllActiveVehicleLocations();
    public void updateVehicleLocation(VehiclePositionDTO vehiclePosition);
}
