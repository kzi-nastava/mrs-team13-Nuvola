package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.exceptions.ride.InvalidRideStateException;
import Nuvola.Projekatsiit2025.exceptions.ride.RideNotFoundException;
import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.enums.DriverStatus;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import Nuvola.Projekatsiit2025.repositories.DriverRepository;
import Nuvola.Projekatsiit2025.repositories.RideRepository;
import Nuvola.Projekatsiit2025.services.impl.RideServiceImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RideServiceStopRideTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private RideServiceImpl rideService;

    @Test
    void stopRide_finishesInProgressRideAndActivatesDriver() {
        Driver driver = newDriver(10L, DriverStatus.BUSY);
        Ride ride = newRide(100L, 900.0, RideStatus.IN_PROGRESS, driver);

        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(ride)).thenReturn(ride);

        Ride result = rideService.stopRide(100L);

        assertThat(result).isSameAs(ride);
        assertThat(ride.getStatus()).isEqualTo(RideStatus.FINISHED);
        assertThat(ride.getEndTime()).isNotNull();
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.ACTIVE);

        verify(rideRepository).findById(100L);
        verify(driverRepository).save(driver);
        verify(rideRepository).save(ride);
    }

    @Test
    @Tag("exception")
    void stopRide_throwsRideNotFound_whenRideDoesNotExist() {
        when(rideRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.stopRide(404L))
                .isInstanceOf(RideNotFoundException.class);

        verify(rideRepository).findById(404L);
        verify(rideRepository, never()).save(any(Ride.class));
        verifyNoInteractions(driverRepository);
    }

    @Test
    @Tag("exception")
    void stopRide_throwsInvalidRideState_whenRideIsNotInProgress() {
        Ride ride = newRide(101L, 1200.0, RideStatus.SCHEDULED, newDriver(10L, DriverStatus.ACTIVE));
        when(rideRepository.findById(101L)).thenReturn(Optional.of(ride));

        assertThatThrownBy(() -> rideService.stopRide(101L))
                .isInstanceOf(InvalidRideStateException.class);

        assertThat(ride.getStatus()).isEqualTo(RideStatus.SCHEDULED);
        assertThat(ride.getEndTime()).isNull();
        verify(rideRepository).findById(101L);
        verify(rideRepository, never()).save(any(Ride.class));
        verifyNoInteractions(driverRepository);
    }

    private Driver newDriver(Long id, DriverStatus status) {
        Driver driver = new Driver();
        driver.setId(id);
        driver.setUsername("marko");
        driver.setEmail("marko@mail.com");
        driver.setPassword("pass");
        driver.setFirstName("Ime");
        driver.setLastName("Prezime");
        driver.setAddress("Adresa");
        driver.setPhone("000");
        driver.setBlocked(false);
        driver.setStatus(status);
        return driver;
    }

    private Ride newRide(Long id, double price, RideStatus status, Driver driver) {
        Ride ride = new Ride();
        ride.setId(id);
        ride.setPrice(price);
        ride.setStatus(status);
        ride.setDriver(driver);
        ride.setCreationTime(LocalDateTime.now().minusMinutes(30));
        ride.setStartTime(LocalDateTime.now().minusMinutes(10));
        return ride;
    }
}
