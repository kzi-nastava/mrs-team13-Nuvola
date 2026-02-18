package Nuvola.Projekatsiit2025.controllers;

import Nuvola.Projekatsiit2025.controller.RideController;
import Nuvola.Projekatsiit2025.dto.CreateRideDTO;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import Nuvola.Projekatsiit2025.model.enums.VehicleType;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import Nuvola.Projekatsiit2025.services.RideEstimateService;
import Nuvola.Projekatsiit2025.services.RideService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RideController.class)
@AutoConfigureMockMvc(addFilters = false)
class RideControllerCreateRideTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RideService rideService;

    @MockBean
    private RideEstimateService rideEstimateService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void createRide_returns201_whenSuccess() throws Exception {

        Ride ride = new Ride();
        ride.setId(10L);
        ride.setStatus(RideStatus.SCHEDULED);
        ride.setPrice(1450);

        when(rideService.createRide(any(), any())).thenReturn(ride);

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
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void createRide_returns400_whenServiceThrows() throws Exception {

        when(rideService.createRide(any(), any()))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "NO_AVAILABLE_DRIVER"
                ));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "from":{"latitude":45,"longitude":19},
                      "to":{"latitude":46,"longitude":20},
                      "vehicleType":"STANDARD"
                    }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("NO_AVAILABLE_DRIVER"));
    }

    @Test
    void createRide_returns403_whenUserBlocked() throws Exception {
        when(rideService.createRide(any(), any()))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN,
                        "ACCOUNT_BLOCKED"
                ));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "from":{"latitude":45,"longitude":19},
                          "to":{"latitude":46,"longitude":20},
                          "vehicleType":"STANDARD"
                        }
                    """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("ACCOUNT_BLOCKED"));
    }

    @Test
    void createRide_returns400_whenNoActiveDrivers() throws Exception {
        when(rideService.createRide(any(), any()))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "NO_ACTIVE_DRIVERS"
                ));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "from":{"latitude":45,"longitude":19},
                          "to":{"latitude":46,"longitude":20},
                          "vehicleType":"STANDARD"
                        }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("NO_ACTIVE_DRIVERS"));
    }
}