package Nuvola.Projekatsiit2025.controllers;

import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.enums.DriverStatus;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import Nuvola.Projekatsiit2025.repositories.DriverRepository;
import Nuvola.Projekatsiit2025.repositories.RideRepository;
import Nuvola.Projekatsiit2025.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@ActiveProfiles("test")
public class RideControllerIntegrationStopRideTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private DriverRepository driverRepository;

    @MockBean
    private EmailService emailService;

    private Driver driver;

    @BeforeEach
    void setUp() {
        rideRepository.deleteAll();
        driverRepository.deleteAll();
        driver = driverRepository.saveAndFlush(newDriver("marko", DriverStatus.BUSY));
    }

    @Test
    @Tag("integration")
    @DisplayName("PUT /api/rides/{rideId}/stop -> 200 and persists FINISHED status")
    void stopRide_200_whenRideIsInProgress() throws Exception {
        Ride ride = rideRepository.saveAndFlush(newRide(RideStatus.IN_PROGRESS, driver));

        mockMvc.perform(put("/api/rides/{rideId}/stop", ride.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ride.getId()))
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.price").value(900.0));

        Ride persistedRide = rideRepository.findById(ride.getId()).orElseThrow();
        Driver persistedDriver = driverRepository.findById(driver.getId()).orElseThrow();
        assertThat(persistedRide.getStatus()).isEqualTo(RideStatus.FINISHED);
        assertThat(persistedRide.getEndTime()).isNotNull();
        assertThat(persistedDriver.getStatus()).isEqualTo(DriverStatus.ACTIVE);
    }

    @Test
    @Tag("integration")
    @Tag("exception")
    @DisplayName("PUT /api/rides/{rideId}/stop -> 404 when ride does not exist")
    void stopRide_404_whenRideDoesNotExist() throws Exception {
        mockMvc.perform(put("/api/rides/{rideId}/stop", 9999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Tag("integration")
    @Tag("exception")
    @DisplayName("PUT /api/rides/{rideId}/stop -> 400 when ride is not active")
    void stopRide_400_whenRideIsNotInProgress() throws Exception {
        Ride ride = rideRepository.saveAndFlush(newRide(RideStatus.SCHEDULED, driver));

        mockMvc.perform(put("/api/rides/{rideId}/stop", ride.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Ride persistedRide = rideRepository.findById(ride.getId()).orElseThrow();
        assertThat(persistedRide.getStatus()).isEqualTo(RideStatus.SCHEDULED);
        assertThat(persistedRide.getEndTime()).isNull();
    }

    private Driver newDriver(String username, DriverStatus status) {
        Driver d = new Driver();
        d.setUsername(username);
        d.setEmail(username + "@mail.com");
        d.setPassword("pass");
        d.setFirstName("Ime");
        d.setLastName("Prezime");
        d.setAddress("Adresa");
        d.setPhone("000");
        d.setBlocked(false);
        d.setStatus(status);
        return d;
    }

    private Ride newRide(RideStatus status, Driver driver) {
        Ride r = new Ride();
        r.setPrice(900.0);
        r.setStatus(status);
        r.setDriver(driver);
        r.setCreationTime(LocalDateTime.now().minusMinutes(30));
        r.setStartTime(LocalDateTime.now().minusMinutes(10));
        r.setPanic(false);
        return r;
    }
}
