package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.DriverRideHistoryItemDTO;

import java.util.List;

public interface RideService {
    public List<DriverRideHistoryItemDTO> getDriverRideHistory(Long driverId, String sortBy, String sortOrder);

}
