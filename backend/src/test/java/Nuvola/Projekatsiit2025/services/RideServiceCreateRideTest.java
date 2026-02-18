package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.CoordinateDTO;
import Nuvola.Projekatsiit2025.dto.CreateRideDTO;
import Nuvola.Projekatsiit2025.model.*;
import Nuvola.Projekatsiit2025.model.enums.*;
import Nuvola.Projekatsiit2025.repositories.*;
import Nuvola.Projekatsiit2025.services.impl.RideServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RideServiceCreateRideTest {

    @Mock private RideRepository rideRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private UserRepository userRepository;
    @Mock private RouteRepository routeRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private RideServiceImpl rideService;

    private RegisteredUser user;
    private CreateRideDTO dto;

    @BeforeEach
    void setup() {
        user = new RegisteredUser();
        user.setId(1L);
        user.setBlocked(false);

        dto = new CreateRideDTO();
        dto.setVehicleType(VehicleType.STANDARD);
        dto.setFrom(new CoordinateDTO(45.0,19.0));
        dto.setTo(new CoordinateDTO(46.0,20.0));
        dto.setScheduledTime(LocalDateTime.now());
    }

    @Test
    void createRide_throwsForbidden_whenUserBlocked() {
        user.setBlocked(true);
        user.setBlockingReason("BAD_BEHAVIOR");

        assertThatThrownBy(() -> rideService.createRide(user, dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("ACCOUNT_BLOCKED");
    }

    @Test
    void createRide_throwsBadRequest_whenNoActiveDrivers() {
        when(driverRepository.findActiveAndBusyDriversWithVehicle())
                .thenReturn(List.of());

        assertThatThrownBy(() -> rideService.createRide(user, dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("NO_ACTIVE_DRIVERS");

        verify(notificationService).sendNotification(
                eq(1L), anyString(), anyString(), eq(NotificationType.NoVehicleAvailable)
        );
    }


    @Test
    void createRide_throwsBadRequest_whenNoMatchingDriver() {
        Driver driver = new Driver();
        driver.setBlocked(false);
        driver.setVehicle(new Vehicle());
        driver.getVehicle().setType(VehicleType.LUXURY);

        when(driverRepository.findActiveAndBusyDriversWithVehicle())
                .thenReturn(List.of(driver));

        when(driverRepository.findActiveDriversWithVehicle())
                .thenReturn(List.of(driver));

        assertThatThrownBy(() -> rideService.createRide(user, dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("NO_AVAILABLE_DRIVER");
    }

    @Test
    void createRide_successfullyCreatesRide() {
        Driver driver = new Driver();
        driver.setId(10L);
        driver.setBlocked(false);
        driver.setStatus(DriverStatus.ACTIVE);

        Vehicle vehicle = new Vehicle();
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setBabyFriendly(true);
        vehicle.setPetFriendly(true);

        driver.setVehicle(vehicle);

        when(driverRepository.findActiveAndBusyDriversWithVehicle())
                .thenReturn(List.of(driver));

        when(driverRepository.findActiveDriversWithVehicle())
                .thenReturn(List.of(driver));

        when(routeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rideRepository.save(any())).thenAnswer(inv -> {
            Ride r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });

        Ride result = rideService.createRide(user, dto);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(RideStatus.SCHEDULED);
        assertThat(result.getDriver()).isEqualTo(driver);

        verify(notificationService).sendNotification(
                eq(1L), anyString(), anyString(), eq(NotificationType.RideApproved)
        );
        verify(notificationService).sendNotification(
                eq(10L), anyString(), anyString(), eq(NotificationType.YouAreAssignedToRide)
        );
    }

    @Test
    void createRide_fails_whenBabyTransportRequestedButDriverNotBabyFriendly() {

        Driver driver = new Driver();
        driver.setBlocked(false);
        driver.setStatus(DriverStatus.ACTIVE);

        Vehicle vehicle = new Vehicle();
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setBabyFriendly(false);
        vehicle.setPetFriendly(true);

        driver.setVehicle(vehicle);

        dto.setBabyTransport(true);

        when(driverRepository.findActiveAndBusyDriversWithVehicle())
                .thenReturn(List.of(driver));

        when(driverRepository.findActiveDriversWithVehicle())
                .thenReturn(List.of(driver));

        assertThatThrownBy(() -> rideService.createRide(user, dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("NO_AVAILABLE_DRIVER");
    }

    @Test
    void createRide_fails_whenPetTransportRequestedButDriverNotPetFriendly() {

        Driver driver = new Driver();
        driver.setBlocked(false);
        driver.setStatus(DriverStatus.ACTIVE);

        Vehicle vehicle = new Vehicle();
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setBabyFriendly(true);
        vehicle.setPetFriendly(false);

        driver.setVehicle(vehicle);

        dto.setPetTransport(true);

        when(driverRepository.findActiveAndBusyDriversWithVehicle())
                .thenReturn(List.of(driver));

        when(driverRepository.findActiveDriversWithVehicle())
                .thenReturn(List.of(driver));

        assertThatThrownBy(() -> rideService.createRide(user, dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("NO_AVAILABLE_DRIVER");
    }

    @Test
    void createRide_allowsNullScheduledTime() {

        dto.setScheduledTime(null);

        Driver driver = new Driver();
        driver.setId(10L);
        driver.setBlocked(false);
        driver.setStatus(DriverStatus.ACTIVE);

        Vehicle vehicle = new Vehicle();
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setBabyFriendly(true);
        vehicle.setPetFriendly(true);

        driver.setVehicle(vehicle);

        when(driverRepository.findActiveAndBusyDriversWithVehicle())
                .thenReturn(List.of(driver));

        when(driverRepository.findActiveDriversWithVehicle())
                .thenReturn(List.of(driver));

        when(routeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rideRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Ride ride = rideService.createRide(user, dto);

        assertThat(ride.getStartTime()).isNull();
    }
}