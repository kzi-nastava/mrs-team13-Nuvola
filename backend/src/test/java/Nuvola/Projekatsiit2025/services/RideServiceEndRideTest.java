package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.exceptions.ride.InvalidRideStateException;
import Nuvola.Projekatsiit2025.exceptions.ride.RideNotFoundException;
import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.RegisteredUser;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.enums.DriverStatus;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import Nuvola.Projekatsiit2025.repositories.DriverRepository;
import Nuvola.Projekatsiit2025.repositories.RideRepository;
import Nuvola.Projekatsiit2025.services.impl.RideServiceImpl;
import Nuvola.Projekatsiit2025.util.EmailDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RideServiceEndRideTest {

    @Mock
    private RideRepository rideRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RideServiceImpl rideService;

    private String driverUsername;

    @BeforeEach
    void setUp() {
        driverUsername = "marko";
    }

    @Test
    void endRide_throwsRideNotFound_whenNoInProgressRide() {
        when(rideRepository.findByDriver_UsernameAndStatus(driverUsername, RideStatus.IN_PROGRESS))
                .thenReturn(List.of());

        assertThatThrownBy(() -> rideService.endRide(driverUsername))
                .isInstanceOf(RideNotFoundException.class);

        verify(rideRepository, times(1))
                .findByDriver_UsernameAndStatus(driverUsername, RideStatus.IN_PROGRESS);
        verifyNoInteractions(emailService);
    }

    @Test
    void endRide_throwsInvalidRideState_whenMultipleInProgressRides() {
        Ride r1 = new Ride();
        Ride r2 = new Ride();

        when(rideRepository.findByDriver_UsernameAndStatus(driverUsername, RideStatus.IN_PROGRESS))
                .thenReturn(List.of(r1, r2));

        assertThatThrownBy(() -> rideService.endRide(driverUsername))
                .isInstanceOf(InvalidRideStateException.class);

        verify(rideRepository, times(1))
                .findByDriver_UsernameAndStatus(driverUsername, RideStatus.IN_PROGRESS);
        verifyNoInteractions(emailService);
    }

    @Test
    void endRide_returnsNull_whenNoNearestScheduledRide() {
        // arrange: 1 in-progress ride
        Driver driver = newDriver(10L, driverUsername, "driver@mail.com", DriverStatus.BUSY);

        RegisteredUser creator = newRegisteredUser(1L, "creator@mail.com");
        RegisteredUser p1 = newRegisteredUser(2L, "p1@mail.com");
        RegisteredUser p2 = newRegisteredUser(3L, "p2@mail.com");

        Ride inProgress = newRide(100L, 1234.0, RideStatus.IN_PROGRESS, driver, creator, List.of(p1, p2));

        when(rideRepository.findByDriver_UsernameAndStatus(driverUsername, RideStatus.IN_PROGRESS))
                .thenReturn(List.of(inProgress));

        // nearest scheduled: empty
        when(rideRepository
                .findFirstByDriverIdAndStatusAndStartTimeIsNotNullAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
                        eq(driver.getId()), eq(RideStatus.SCHEDULED), any(LocalDateTime.class)
                ))
                .thenReturn(Optional.empty());

        // “snapshot” hvatanje email detalja u trenutku poziva (jer se isti EmailDetails objekat reciklira u petlji)
        List<String> recipients = new ArrayList<>();
        List<String> subjects = new ArrayList<>();
        List<String> bodies = new ArrayList<>();

        doAnswer(inv -> {
            EmailDetails ed = inv.getArgument(0);
            recipients.add(ed.getRecipient());
            subjects.add(ed.getSubject());
            bodies.add(ed.getMsgBody());
            return null;
        }).when(emailService).sendRideFinished(any(EmailDetails.class));

        // act
        Long result = rideService.endRide(driverUsername);

        // assert
        assertThat(result).isNull();

        // ride + driver state changed
        assertThat(inProgress.getStatus()).isEqualTo(RideStatus.FINISHED);
        assertThat(inProgress.getEndTime()).isNotNull();
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.ACTIVE);

        // emails: 2 passengers + creator = 3
        assertThat(recipients).containsExactlyInAnyOrder("p1@mail.com", "p2@mail.com", "creator@mail.com");
        assertThat(subjects).allMatch("Ride Ended"::equals);
        assertThat(bodies).allMatch(b -> b.contains("Ride ID: 100") && b.contains("Price: 1234.0"));

        verify(rideRepository, times(1))
                .findByDriver_UsernameAndStatus(driverUsername, RideStatus.IN_PROGRESS);

        verify(rideRepository, times(1))
                .findFirstByDriverIdAndStatusAndStartTimeIsNotNullAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
                        eq(driver.getId()), eq(RideStatus.SCHEDULED), any(LocalDateTime.class)
                );

        verify(emailService, times(3)).sendRideFinished(any(EmailDetails.class));
    }

    @Test
    void endRide_returnsNearestScheduledRideId_whenExists() {
        // arrange: 1 in-progress ride
        Driver driver = newDriver(10L, driverUsername, "driver@mail.com", DriverStatus.BUSY);
        RegisteredUser creator = newRegisteredUser(1L, "creator@mail.com");
        RegisteredUser p1 = newRegisteredUser(2L, "p1@mail.com");

        Ride inProgress = newRide(100L, 999.0, RideStatus.IN_PROGRESS, driver, creator, List.of(p1));

        when(rideRepository.findByDriver_UsernameAndStatus(driverUsername, RideStatus.IN_PROGRESS))
                .thenReturn(List.of(inProgress));

        Ride scheduled = new Ride();
        scheduled.setId(777L);
        scheduled.setStatus(RideStatus.SCHEDULED);
        scheduled.setStartTime(LocalDateTime.now().plusMinutes(15));

        when(rideRepository
                .findFirstByDriverIdAndStatusAndStartTimeIsNotNullAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
                        eq(driver.getId()), eq(RideStatus.SCHEDULED), any(LocalDateTime.class)
                ))
                .thenReturn(Optional.of(scheduled));

        // act
        Long result = rideService.endRide(driverUsername);

        // assert
        assertThat(result).isEqualTo(777L);
        assertThat(inProgress.getStatus()).isEqualTo(RideStatus.FINISHED);
        assertThat(inProgress.getEndTime()).isNotNull();
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.ACTIVE);

        verify(emailService, times(2)).sendRideFinished(any(EmailDetails.class)); // 1 passenger + creator
    }

    // -------- helpers (minimal valid objects) --------

    private Driver newDriver(Long id, String username, String email, DriverStatus status) {
        Driver d = new Driver();
        d.setId(id);
        d.setUsername(username);   // polje username
        d.setEmail(email);
        d.setStatus(status);
        d.setBlocked(false);

        // ako imate NOT NULL kolone u users tabeli, setuj i njih:
        d.setPassword("pass");
        d.setFirstName("Ime");
        d.setLastName("Prezime");
        d.setAddress("Adresa");
        d.setPhone("000");
        return d;
    }

    private RegisteredUser newRegisteredUser(Long id, String email) {
        RegisteredUser u = new RegisteredUser();
        u.setId(id);
        u.setEmail(email);
        u.setUsername("u" + id);
        u.setPassword("pass");
        u.setFirstName("Ime");
        u.setLastName("Prezime");
        u.setAddress("Adresa");
        u.setPhone("000");
        u.setBlocked(false);
        u.setActivated(true);
        return u;
    }

    private Ride newRide(Long id, double price, RideStatus status,
                         Driver driver, RegisteredUser creator, List<RegisteredUser> passengers) {
        Ride r = new Ride();
        r.setId(id);
        r.setPrice(price);
        r.setStatus(status);
        r.setDriver(driver);
        r.setCreator(creator);
        r.setOtherPassengers(passengers);
        r.setCreationTime(LocalDateTime.now().minusMinutes(30));
        r.setStartTime(LocalDateTime.now().minusMinutes(10));
        r.setPanic(false);
        return r;
    }
}
