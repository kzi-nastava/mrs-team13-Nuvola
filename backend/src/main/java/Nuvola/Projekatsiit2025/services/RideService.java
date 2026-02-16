package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.CreateReportDTO;
import Nuvola.Projekatsiit2025.dto.CreateRideDTO;
import Nuvola.Projekatsiit2025.dto.DriverRideHistoryItemDTO;
import Nuvola.Projekatsiit2025.dto.ScheduledRideDTO;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RideService {
    public Page<DriverRideHistoryItemDTO> getDriverRideHistory(Long driverId, String sortBy, String sortOrder, Integer page, Integer size);
    public void startRide(Long rideId);
    Ride createRide(User loggedUser, CreateRideDTO dto);
    Long endRide(String username);
    void createReport(CreateReportDTO createReportDTO);
    List<Ride> getAssignedRidesForDriver(String username);
    ScheduledRideDTO getScheduledRide(Long rideId);
    boolean userHasActiveRide(Long userId);
}
