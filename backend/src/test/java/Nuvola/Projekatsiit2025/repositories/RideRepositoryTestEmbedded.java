package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.enums.DriverStatus;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class RideRepositoryTestEmbedded {

    // t2.7b - Test RideRepository methods with embedded database
    // Methods:
    // 1.Optional<Ride> findFirstByDriverIdAndStatusAndStartTimeIsNotNullAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
    //            Long driverId, RideStatus status, LocalDateTime time
    //    );
    // 2. List<Ride> findByDriver_UsernameAndStatus(String username, RideStatus status);

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private TestEntityManager em;

    // -------------------- TEST 1 --------------------
    @Test
    void findFirstByDriverIdAndStatusAndStartTimeIsNotNullAndStartTimeGreaterThanEqualOrderByStartTimeAsc_returnsEarliestMatchingRide() {
        Driver driver = persistDriver("marko", "marko@mail.com");
        Driver otherDriver = persistDriver("jovan", "jovan@mail.com");

        LocalDateTime threshold = LocalDateTime.of(2026, 1, 10, 10, 0);

        // Ne treba: startTime null
        persistRide(driver, RideStatus.SCHEDULED, null);

        // Ne treba: pre threshold
        persistRide(driver, RideStatus.SCHEDULED, threshold.minusMinutes(1));

        // Ne treba: pogrešan status
        persistRide(driver, RideStatus.CANCELED, threshold.plusMinutes(5));

        // Ne treba: drugi vozač
        persistRide(otherDriver, RideStatus.SCHEDULED, threshold.plusMinutes(1));

        // Treba: kandidati (treba da vrati NAJRANIJI startTime >= threshold)
        Ride expected = persistRide(driver, RideStatus.SCHEDULED, threshold.plusMinutes(1));
        persistRide(driver, RideStatus.SCHEDULED, threshold.plusMinutes(2));
        persistRide(driver, RideStatus.SCHEDULED, threshold.plusMinutes(10));

        em.flush();
        em.clear();

        Optional<Ride> result =
                rideRepository.findFirstByDriverIdAndStatusAndStartTimeIsNotNullAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
                        driver.getId(), RideStatus.SCHEDULED, threshold
                );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(expected.getId());
        assertThat(result.get().getStartTime()).isEqualTo(threshold.plusMinutes(1));
    }

    @Test
    void findFirstByDriverIdAndStatusAndStartTimeIsNotNullAndStartTimeGreaterThanEqualOrderByStartTimeAsc_returnsEmptyWhenNoMatch() {
        Driver driver = persistDriver("marko", "marko@mail.com");
        LocalDateTime threshold = LocalDateTime.of(2026, 1, 10, 10, 0);

        // Sve ne odgovara
        persistRide(driver, RideStatus.SCHEDULED, threshold.minusHours(1)); // pre threshold
        persistRide(driver, RideStatus.CANCELED, threshold.plusMinutes(5)); // pogrešan status
        persistRide(driver, RideStatus.SCHEDULED, null);                    // null startTime

        em.flush();
        em.clear();

        Optional<Ride> result =
                rideRepository.findFirstByDriverIdAndStatusAndStartTimeIsNotNullAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
                        driver.getId(), RideStatus.SCHEDULED, threshold
                );

        assertThat(result).isEmpty();
    }

    // -------------------- TEST 2 --------------------
    @Test
    void findByDriver_UsernameAndStatus_returnsOnlyRidesForThatUsernameAndStatus() {
        Driver d1 = persistDriver("marko", "marko@mail.com");
        Driver d2 = persistDriver("jovan", "jovan@mail.com");

        Ride a = persistRide(d1, RideStatus.FINISHED, LocalDateTime.now().minusHours(3));
        Ride b = persistRide(d1, RideStatus.FINISHED, LocalDateTime.now().minusHours(1));

        // Ne treba: isti driver, drugi status
        persistRide(d1, RideStatus.CANCELED, LocalDateTime.now().plusDays(1));

        // Ne treba: drugi driver, isti status
        persistRide(d2, RideStatus.FINISHED, LocalDateTime.now().minusHours(2));

        em.flush();
        em.clear();

        List<Ride> result = rideRepository.findByDriver_UsernameAndStatus("marko", RideStatus.FINISHED);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Ride::getId).containsExactlyInAnyOrder(a.getId(), b.getId());
    }

    @Test
    void findByDriver_UsernameAndStatus_returnsEmptyWhenUsernameOrStatusDoesNotMatch() {
        Driver d1 = persistDriver("marko", "marko@mail.com");
        persistRide(d1, RideStatus.FINISHED, LocalDateTime.now().minusHours(2));

        em.flush();
        em.clear();

        assertThat(rideRepository.findByDriver_UsernameAndStatus("nepostoji", RideStatus.FINISHED)).isEmpty();
        assertThat(rideRepository.findByDriver_UsernameAndStatus("marko", RideStatus.CANCELED)).isEmpty();
    }

    // -------------------- HELPERS --------------------
    private Driver persistDriver(String username, String email) {
        Driver d = new Driver();
        d.setUsername(username);        // polje u bazi
        d.setEmail(email);
        d.setPassword("pass");
        d.setFirstName("Ime");
        d.setLastName("Prezime");
        d.setAddress("Adresa");
        d.setPhone("000");
        d.setBlocked(false);
        d.setStatus(DriverStatus.ACTIVE);  // bitno jer je nullable=false

        return em.persist(d);
    }

    private Ride persistRide(Driver driver, RideStatus status, LocalDateTime startTime) {
        Ride r = new Ride();
        r.setPrice(100);
        r.setStatus(status);
        r.setDriver(driver);
        r.setStartTime(startTime);
        r.setCreationTime(LocalDateTime.now());
        r.setPanic(false);
        return em.persist(r);
    }
}
