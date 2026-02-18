package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.*;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface RideService {
    public Page<DriverRideHistoryItemDTO> getDriverRideHistory(String username, String sortBy, String sortOrder, Integer page, Integer size);
    public void startRide(Long rideId);
    Ride createRide(User loggedUser, CreateRideDTO dto);
    Long endRide(String username);
    void createReport(CreateReportDTO createReportDTO);
    List<Ride> getAssignedRidesForDriver(String username);
    ScheduledRideDTO getScheduledRide(Long rideId);
    boolean userHasActiveRide(Long userId);
    void triggerPanic(Long rideId, Long userId);
    List<PanicDTO> getActivePanicNotifications();
  Page<RegisteredUserRideHistoryItemDTO> getUserRideHistory(Long userId, LocalDateTime from, LocalDateTime to, String sortBy, String sortOrder, Integer page, Integer size);
    RideHistoryDetailsDTO getRideHistoryDetailsForUser(Long rideId, Long userId);
    boolean toggleFavoriteRouteForUser(Long rideId, Long id);
    Ride createRideFromHistory(User loggedUser, CreateRideFromHistoryDTO dto);
    TrackingRideDTO getTrackingRideDTO(String username);
    TrackingRideDTO getTrackingRideDTOForAdmin(Long driverId);
    Ride stopRide(Long rideId, User currentUser, StopRideRequestDTO req);
}