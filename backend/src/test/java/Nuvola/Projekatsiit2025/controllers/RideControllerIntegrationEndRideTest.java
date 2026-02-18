package Nuvola.Projekatsiit2025.controllers;

import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.RegisteredUser;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.enums.DriverStatus;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import Nuvola.Projekatsiit2025.repositories.DriverRepository;
import Nuvola.Projekatsiit2025.repositories.RegisteredUserRepository;
import Nuvola.Projekatsiit2025.repositories.RideRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@ActiveProfiles("test")
public class RideControllerIntegrationEndRideTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private DriverRepository driverRepository;
    @Autowired private RideRepository rideRepository;
    @Autowired private RegisteredUserRepository registeredUserRepository;

    private Driver marko;

    @AfterAll
    static void tearDown( @Autowired RideRepository rideRepository,
                         @Autowired DriverRepository driverRepository,
                         @Autowired RegisteredUserRepository registeredUserRepository) {
        rideRepository.deleteAll();
        driverRepository.deleteAll();
        registeredUserRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        rideRepository.deleteAll();
        driverRepository.deleteAll();
        registeredUserRepository.deleteAll();

        marko = new Driver();
        marko.setUsername("marko");
        marko.setEmail("marko@mail.com");
        marko.setPassword("pass");
        marko.setFirstName("Ime");
        marko.setLastName("Prezime");
        marko.setAddress("Adresa");
        marko.setPhone("000");
        marko.setBlocked(false);
        marko.setStatus(DriverStatus.ACTIVE);

        marko = driverRepository.saveAndFlush(marko);
    }

    @Test
    @Tag("exception")
    @Tag("integration")
    @DisplayName("PUT /api/rides/{username}/end -> 404 when ride doesn't exist")
    void endRide_404_whenMissing() throws Exception {
        mockMvc.perform(put("/api/rides/{username}/end", "marko")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Tag("integration")
    @DisplayName("PUT /api/rides/{username}/end -> 204")
    void endRide_204_whenScheduledRideDoesntExist() throws Exception {

        RegisteredUser creator = registeredUserRepository.saveAndFlush(newRegisteredUser("creator@mail.com"));
        RegisteredUser p1 = registeredUserRepository.saveAndFlush(newRegisteredUser("p1@mail.com"));
        RegisteredUser p2 = registeredUserRepository.saveAndFlush(newRegisteredUser("p2@mail.com"));


        Ride inProgress = rideRepository.saveAndFlush(newRide(1234.0, RideStatus.IN_PROGRESS, marko, creator, List.of(p1, p2)));


        Ride r = new Ride();
        r.setPrice(100);
        r.setDriver(marko);
        r.setStatus(RideStatus.SCHEDULED);
        r.setStartTime(LocalDateTime.now().minusMinutes(10));
        r.setCreationTime(LocalDateTime.now().minusMinutes(20));
        r.setPanic(false);
        r = rideRepository.saveAndFlush(r);

        mockMvc.perform(put("/api/rides/{username}/end", "marko")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @Tag("integration")
    @DisplayName("PUT /api/rides/{username}/end -> 200")
    void endRide_200_whenScheduledRideExists() throws Exception {

        RegisteredUser creator = registeredUserRepository.saveAndFlush(newRegisteredUser("creator@mail.com"));
        RegisteredUser p1 = registeredUserRepository.saveAndFlush(newRegisteredUser("p1@mail.com"));
        RegisteredUser p2 = registeredUserRepository.saveAndFlush(newRegisteredUser("p2@mail.com"));


        Ride inProgress = rideRepository.saveAndFlush(newRide(1234.0, RideStatus.IN_PROGRESS, marko, creator, List.of(p1, p2)));


        Ride r = new Ride();
        r.setPrice(100);
        r.setDriver(marko);
        r.setStatus(RideStatus.SCHEDULED);
        r.setCreationTime(LocalDateTime.now().plusMinutes(40));
        r.setStartTime(LocalDateTime.now().plusMinutes(50));
        r.setPanic(false);
        r = rideRepository.saveAndFlush(r);

        mockMvc.perform(put("/api/rides/{username}/end", "marko")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(String.valueOf(r.getId())));
    }


    private Driver newDriver(Long id, String username, String email, DriverStatus status) {
        Driver d = new Driver();
        d.setId(id);
        d.setUsername(username);
        d.setEmail(email);
        d.setStatus(status);
        d.setBlocked(false);
        d.setPassword("pass");
        d.setFirstName("Ime");
        d.setLastName("Prezime");
        d.setAddress("Adresa");
        d.setPhone("000");
        return d;
    }

    private RegisteredUser newRegisteredUser(String email) {
        RegisteredUser u = new RegisteredUser();
        u.setEmail(email);
        u.setUsername("u" + email);
        u.setPassword("pass");
        u.setFirstName("Ime");
        u.setLastName("Prezime");
        u.setAddress("Adresa");
        u.setPhone("000");
        u.setBlocked(false);
        u.setActivated(true);
        return u;
    }
    private Ride newRide(double price, RideStatus status,
                         Driver driver, RegisteredUser creator, List<RegisteredUser> passengers) {
        Ride r = new Ride();
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
