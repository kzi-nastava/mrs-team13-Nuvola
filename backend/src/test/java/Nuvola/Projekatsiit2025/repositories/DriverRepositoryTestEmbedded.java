package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.Vehicle;
import Nuvola.Projekatsiit2025.model.enums.DriverStatus;
import Nuvola.Projekatsiit2025.model.enums.VehicleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class DriverRepositoryTestEmbedded {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private TestEntityManager em;

    private int counter = 0;

    @Test
    void findActiveDriversWithVehicle_returnsOnlyActiveWithVehicle() {

        Driver active = persistDriver(DriverStatus.ACTIVE, true);
        persistDriver(DriverStatus.BUSY, true);
        persistDriver(DriverStatus.ACTIVE, false);

        var result = driverRepository.findActiveDriversWithVehicle();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(active.getId());
    }


    private Driver persistDriver(DriverStatus status, boolean hasVehicle) {

        counter++;

        String email = "driver" + counter + "@mail.com";

        Driver d = new Driver();
        d.setEmail(email);
        d.setUsername(email);

        d.setPassword("pass");
        d.setFirstName("Ime");
        d.setLastName("Prezime");
        d.setAddress("Adr");
        d.setPhone("000");
        d.setBlocked(false);
        d.setStatus(status);

        if (hasVehicle) {
            Vehicle v = new Vehicle();
            v.setType(VehicleType.STANDARD);
            v.setBabyFriendly(true);
            v.setPetFriendly(true);
            v.setRegNumber("REG-" + counter);
            v.setModel("Toyota");
            v.setNumOfSeats(4);
            d.setVehicle(v);
        }

        return em.persist(d);
    }

    @Test
    void findActiveAndBusyDriversWithVehicle_returnsActiveAndBusyOnly() {

        persistDriver(DriverStatus.ACTIVE, true);
        persistDriver(DriverStatus.BUSY, true);
        persistDriver(DriverStatus.INACTIVE, true);
        persistDriver(DriverStatus.ACTIVE, false);

        var result = driverRepository.findActiveAndBusyDriversWithVehicle();

        assertThat(result).hasSize(2);
    }
}