package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.DriverRideHistoryItemDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RideService {
    public Page<DriverRideHistoryItemDTO> getDriverRideHistory(Long driverId, String sortBy, String sortOrder, Integer page, Integer size);
    public void startRide(Long rideId);

}
