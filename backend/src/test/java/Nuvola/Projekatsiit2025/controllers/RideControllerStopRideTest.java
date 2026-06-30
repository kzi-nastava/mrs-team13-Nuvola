package Nuvola.Projekatsiit2025.controllers;

import Nuvola.Projekatsiit2025.controller.RideController;
import Nuvola.Projekatsiit2025.exceptions.ride.InvalidRideStateException;
import Nuvola.Projekatsiit2025.exceptions.ride.RideNotFoundException;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import Nuvola.Projekatsiit2025.services.impl.RideEstimateServiceImpl;
import Nuvola.Projekatsiit2025.services.impl.RideServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = RideController.class)
public class RideControllerStopRideTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RideServiceImpl rideService;

    @MockBean
    private RideEstimateServiceImpl rideEstimateService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("PUT /api/rides/{rideId}/stop -> 200 OK when ride is in progress")
    void stopRide_returnsOk_whenServiceStopsRide() throws Exception {
        Ride stoppedRide = new Ride();
        stoppedRide.setId(123L);
        stoppedRide.setPrice(900.0);
        stoppedRide.setStatus(RideStatus.FINISHED);
        stoppedRide.setCreationTime(LocalDateTime.now().minusMinutes(30));

        Mockito.when(rideService.stopRide(eq(123L))).thenReturn(stoppedRide);

        mockMvc.perform(put("/api/rides/{rideId}/stop", 123L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(123L))
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.price").value(900.0))
                .andExpect(jsonPath("$.message").value("Ride stopped successfully"));

        verify(rideService).stopRide(123L);
    }

    @Test
    @DisplayName("PUT /api/rides/{rideId}/stop -> 404 when ride does not exist")
    void stopRide_returnsNotFound_whenServiceThrowsRideNotFound() throws Exception {
        Mockito.when(rideService.stopRide(eq(404L)))
                .thenThrow(new RideNotFoundException("Ride 404 not found"));

        mockMvc.perform(put("/api/rides/{rideId}/stop", 404L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(rideService).stopRide(404L);
    }

    @Test
    @DisplayName("PUT /api/rides/{rideId}/stop -> 400 when ride is not in progress")
    void stopRide_returnsBadRequest_whenRideIsNotInProgress() throws Exception {
        Mockito.when(rideService.stopRide(eq(321L)))
                .thenThrow(new InvalidRideStateException("Ride 321 is not in progress"));

        mockMvc.perform(put("/api/rides/{rideId}/stop", 321L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(rideService).stopRide(321L);
    }
}
