package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.*;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RideService {
    public Page<DriverRideHistoryItemDTO> getDriverRideHistory(String username, String sortBy, String sortOrder, Integer page, Integer size);
    public void startRide(Long rideId);
    Ride createRide(User loggedUser, CreateRideDTO dto);
    Long endRide(String username);
    void createReport(CreateReportDTO createReportDTO);
    List<Ride> getAssignedRidesForDriver(String username);
    ScheduledRideDTO getScheduledRide(Long rideId);
    void triggerPanic(Long rideId, Long userId);
    List<PanicDTO> getActivePanicNotifications();
}
