package com.example.taxi.controller;

import com.example.taxi.dto.RideDTO;
import com.example.taxi.dto.StopRideRequest;
import com.example.taxi.entity.Ride;
import com.example.taxi.entity.RideStatus;
import com.example.taxi.entity.Driver;
import com.example.taxi.entity.Passenger;
import com.example.taxi.repository.RideRepository;
import com.example.taxi.repository.DriverRepository;
import com.example.taxi.repository.PassengerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("REST Integracioni Testovi - Zaustavljanje Vožnje")
class RideControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    private Ride activeRide;
    private Driver driver;
    private Passenger passenger;

    @BeforeEach
    void setUp() {
        // Kreiranje vozača
        driver = new Driver();
        driver.setName("Marko Petrović");
        driver.setEmail("marko@example.com");
        driver.setPhoneNumber("+381641234567");
        driver.setAvailable(false);
        driver = driverRepository.save(driver);

        // Kreiranje putnika
        passenger = new Passenger();
        passenger.setName("Ana Jovanović");
        passenger.setEmail("ana@example.com");
        passenger.setPhoneNumber("+381651234567");
        passenger = passengerRepository.save(passenger);

        // Kreiranje aktivne vožnje
        activeRide = new Ride();
        activeRide.setDriver(driver);
        activeRide.setPassenger(passenger);
        activeRide.setStatus(RideStatus.IN_PROGRESS);
        activeRide.setStartTime(LocalDateTime.now().minusMinutes(15));
        activeRide.setStartLocation("Bulevar kralja Aleksandra 73");
        activeRide.setDestination("Knez Mihailova 1");
        activeRide.setDistance(5.5);
        activeRide = rideRepository.save(activeRide);
    }

    // ========== POZITIVNI TESTOVI ==========

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Pozitivan: Uspešno zaustavljanje vožnje sa HTTP 200")
    void testStopRide_Success_Returns200() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Knez Mihailova 1");
        request.setFinalDistance(5.5);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activeRide.getId()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.endTime").exists())
                .andExpect(jsonPath("$.totalFare").exists())
                .andExpect(jsonPath("$.totalFare").isNumber())
                .andExpect(jsonPath("$.distance").value(5.5));
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    @DisplayName("IT-Pozitivan: Putnik može zaustaviti vožnju")
    void testStopRide_AsPassenger_Success() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Knez Mihailova 1");
        request.setFinalDistance(5.5);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Pozitivan: Provera izračunavanja cene")
    void testStopRide_FareCalculation() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Knez Mihailova 1");
        request.setFinalDistance(10.0);

        // Act
        MvcResult result = mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String responseBody = result.getResponse().getContentAsString();
        RideDTO response = objectMapper.readValue(responseBody, RideDTO.class);

        assertTrue(response.getTotalFare() > 0);
        assertTrue(response.getTotalFare() >= 120.0); // Minimalna cena
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Pozitivan: Provera ažuriranja statusa vozača")
    void testStopRide_DriverStatusUpdate() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Knez Mihailova 1");
        request.setFinalDistance(5.5);

        // Act
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Assert
        Driver updatedDriver = driverRepository.findById(driver.getId()).orElseThrow();
        assertTrue(updatedDriver.isAvailable());
    }

    // ========== NEGATIVNI TESTOVI ==========

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Negativan: Zaustavljanje nepostojeće vožnje - HTTP 404")
    void testStopRide_NotFound_Returns404() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Test Location");
        request.setFinalDistance(5.0);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Vožnja nije pronađena")));
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Negativan: Zaustavljanje već završene vožnje - HTTP 400")
    void testStopRide_AlreadyCompleted_Returns400() throws Exception {
        // Arrange
        activeRide.setStatus(RideStatus.COMPLETED);
        activeRide.setEndTime(LocalDateTime.now().minusMinutes(5));
        rideRepository.save(activeRide);

        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Test Location");
        request.setFinalDistance(5.0);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("već završena")));
    }

    @Test
    @DisplayName("IT-Negativan: Neautorizovan pristup - HTTP 401")
    void testStopRide_Unauthorized_Returns401() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Test Location");
        request.setFinalDistance(5.0);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("IT-Negativan: Zabranjen pristup za ADMIN rolu - HTTP 403")
    void testStopRide_Forbidden_Returns403() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Test Location");
        request.setFinalDistance(5.0);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Negativan: Nevažeći JSON body - HTTP 400")
    void testStopRide_InvalidJson_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Negativan: Prazan request body - HTTP 400")
    void testStopRide_EmptyBody_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Negativan: Negativna distanca - HTTP 400")
    void testStopRide_NegativeDistance_Returns400() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Test Location");
        request.setFinalDistance(-5.0);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    // ========== GRANIČNI SLUČAJEVI ==========

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Granični: Zaustavljanje sa nula kilometara")
    void testStopRide_ZeroDistance() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Ista lokacija");
        request.setFinalDistance(0.0);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distance").value(0.0))
                .andExpect(jsonPath("$.totalFare").value(greaterThanOrEqualTo(120.0))); // Min cena
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Granični: Maksimalna distanca")
    void testStopRide_MaxDistance() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Veoma daleko");
        request.setFinalDistance(1000.0); // 1000 km

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distance").value(1000.0))
                .andExpect(jsonPath("$.totalFare").exists());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Granični: Vrlo kratka vožnja (< 1 minut)")
    void testStopRide_VeryShortDuration() throws Exception {
        // Arrange
        activeRide.setStartTime(LocalDateTime.now().minusSeconds(30));
        rideRepository.save(activeRide);

        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Blizu");
        request.setFinalDistance(0.1);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duration").exists());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Granični: Vrlo duga vožnja (> 24h)")
    void testStopRide_VeryLongDuration() throws Exception {
        // Arrange
        activeRide.setStartTime(LocalDateTime.now().minusHours(25));
        rideRepository.save(activeRide);

        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Veoma daleko");
        request.setFinalDistance(500.0);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duration").value(greaterThan(1440))); // > 24h u minutima
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Granični: ID vožnje = 0")
    void testStopRide_ZeroRideId() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Test");
        request.setFinalDistance(5.0);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Granični: Maksimalna dužina lokacije (255 karaktera)")
    void testStopRide_MaxLocationLength() throws Exception {
        // Arrange
        String longLocation = "A".repeat(255);
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation(longLocation);
        request.setFinalDistance(5.0);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Granični: Prekoračena dužina lokacije (> 255)")
    void testStopRide_LocationTooLong_Returns400() throws Exception {
        // Arrange
        String tooLongLocation = "A".repeat(256);
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation(tooLongLocation);
        request.setFinalDistance(5.0);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ========== IZUZETNI SLUČAJEVI ==========

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Izuzetak: Specijalni karakteri u lokaciji")
    void testStopRide_SpecialCharactersInLocation() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Král'ova 123 (stan 5/b) <test>");
        request.setFinalDistance(5.0);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Izuzetak: Unicode karakteri u lokaciji")
    void testStopRide_UnicodeInLocation() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("北京市 🚕 Москва");
        request.setFinalDistance(5.0);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("IT-Izuzetak: Provera Content-Type header-a")
    void testStopRide_WrongContentType_Returns415() throws Exception {
        // Arrange
        StopRideRequest request = new StopRideRequest();
        request.setEndLocation("Test");
        request.setFinalDistance(5.0);

        // Act & Assert
        mockMvc.perform(put("/api/rides/{rideId}/stop", activeRide.getId())
                        .contentType(MediaType.APPLICATION_XML) // Pogrešan Content-Type
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }
}
