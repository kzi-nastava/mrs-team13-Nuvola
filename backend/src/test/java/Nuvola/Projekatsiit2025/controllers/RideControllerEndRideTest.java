package Nuvola.Projekatsiit2025.controllers;

import Nuvola.Projekatsiit2025.controller.RideController;
import Nuvola.Projekatsiit2025.exceptions.ride.RideNotFoundException;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = RideController.class)
public class RideControllerEndRideTest {

    @Autowired
    private MockMvc mockMvc;

    // RideController ima @Autowired polja, pa moramo mokovati sva koja Spring pokušava da ubaci
    @MockBean
    private RideServiceImpl rideService;

    @MockBean
    private RideEstimateServiceImpl rideEstimateService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("PUT /api/rides/{username}/end -> 200 OK + rideId when service returns id")
    void endRide_returnsOkWithId_whenServiceReturnsId() throws Exception {
        Mockito.when(rideService.endRide(eq("marko"))).thenReturn(123L);

        mockMvc.perform(put("/api/rides/marko/end")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(String.valueOf(123L)));

        verify(rideService).endRide("marko");
    }

    @Test
    @DisplayName("PUT /api/rides/{username}/end -> 204 No Content when service returns null")
    void endRide_returnsNoContent_whenServiceReturnsNull() throws Exception {
        Mockito.when(rideService.endRide(eq("marko"))).thenReturn(null);

        mockMvc.perform(put("/api/rides/marko/end")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(rideService).endRide("marko");
    }

    @Test
    @DisplayName("PUT /api/rides/{username}/end -> 404 if service throws RideNotFoundException")
    void endRide_returnsServerError_whenServiceThrows() throws Exception {
        Mockito.when(rideService.endRide(eq("marko")))
                .thenThrow(new RideNotFoundException("Ride of marko not found"));

        mockMvc.perform(put("/api/rides/marko/end")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(rideService).endRide("marko");
    }
}
