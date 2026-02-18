package Nuvola.Projekatsiit2025.controllers;

import Nuvola.Projekatsiit2025.model.*;
import Nuvola.Projekatsiit2025.model.enums.*;
import Nuvola.Projekatsiit2025.repositories.*;
import Nuvola.Projekatsiit2025.services.EmailService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RideControllerIntegrationCreateRideTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private DriverRepository driverRepository;
    @Autowired private RideRepository rideRepository;
    @Autowired private RegisteredUserRepository registeredUserRepository;
    @MockBean
    private EmailService emailService;

    @BeforeEach
    void setup() {
        rideRepository.deleteAll();
        driverRepository.deleteAll();
        registeredUserRepository.deleteAll();
    }

    @Test
    void createRide_returns201_whenDriverExists() throws Exception {

        RegisteredUser mockUser = new RegisteredUser();
        mockUser.setUsername("testuser");
        mockUser.setEmail("testuser@mail.com");
        mockUser.setPassword("password");
        mockUser.setFirstName("First");
        mockUser.setLastName("Last");
        mockUser.setAddress("Address");
        mockUser.setPhone("000");
        mockUser.setBlocked(false);
        mockUser.setActivated(true);

        registeredUserRepository.saveAndFlush(mockUser);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        Driver driver = new Driver();
        driver.setUsername("d1");
        driver.setEmail("d1@mail.com");
        driver.setPassword("pass");
        driver.setFirstName("Ime");
        driver.setLastName("Prezime");
        driver.setAddress("Adr");
        driver.setPhone("000");
        driver.setBlocked(false);
        driver.setStatus(DriverStatus.ACTIVE);

        Vehicle v = new Vehicle();
        v.setType(VehicleType.STANDARD);
        v.setRegNumber("NS-123-BA");
        v.setBabyFriendly(true);
        v.setPetFriendly(true);
        v.setModel("Toyota");
        v.setNumOfSeats(4);
        driver.setVehicle(v);

        driverRepository.saveAndFlush(driver);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "from":{"latitude":45,"longitude":19},
                          "to":{"latitude":46,"longitude":20},
                          "vehicleType":"STANDARD"
                        }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void createRide_returns400_whenNoDrivers() throws Exception {
        RegisteredUser mockUser = new RegisteredUser();
        mockUser.setUsername("testuser2");
        mockUser.setEmail("testuser2@mail.com");
        mockUser.setPassword("password");
        mockUser.setFirstName("First");
        mockUser.setLastName("Last");
        mockUser.setAddress("Address");
        mockUser.setPhone("000");
        mockUser.setBlocked(false);
        mockUser.setActivated(true);
        RegisteredUser savedUser = registeredUserRepository.saveAndFlush(mockUser);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(savedUser, null, savedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "from":{"latitude":45,"longitude":19},
                          "to":{"latitude":46,"longitude":20},
                          "vehicleType":"STANDARD"
                        }
                    """))
                .andExpect(status().isBadRequest());
    }
}