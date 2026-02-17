package Nuvola.Projekatsiit2025.controller;
import Nuvola.Projekatsiit2025.dto.*;

import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import Nuvola.Projekatsiit2025.services.EmailService;
import Nuvola.Projekatsiit2025.services.RideEstimateService;
import Nuvola.Projekatsiit2025.services.RideService;
import Nuvola.Projekatsiit2025.util.EmailDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import Nuvola.Projekatsiit2025.dto.ApiErrorResponse;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rides")
@CrossOrigin(origins = "http://localhost:4200")
public class RideController {

    @Autowired
    private RideService rideService;

    @Autowired
    private RideEstimateService rideEstimateService;

    @Autowired
    private UserRepository userRepository;



    // 2.1.2 - Estimate ride
    @PostMapping(value = "/estimate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RideEstimateResponseDTO> estimateRide(@RequestBody RideEstimateRequestDTO request) {
        RideEstimateResponseDTO response = rideEstimateService.estimateRide(request);
        return ResponseEntity.ok(response);
    }

    // 2.4.1
    @PostMapping
    public ResponseEntity<?> createRide(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateRideDTO dto) {

        try {
            Ride ride = rideService.createRide(user, dto);

            CreatedRideDTO response = new CreatedRideDTO();
            response.setId(ride.getId());
            response.setStatus(ride.getStatus());
            response.setPrice(ride.getPrice());
            response.setMessage("Ride successfully created");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (ResponseStatusException e) {
            String reason = e.getReason() != null ? e.getReason() : "ERROR";

            ApiErrorResponse error = new ApiErrorResponse(
                    e.getStatusCode().toString(),
                    reason
            );

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(error);
        }
    }

    // 2.4.3
    @PostMapping(value = "/from-favorite/{favoriteRouteId}")
    public ResponseEntity<CreatedRideDTO> createRideFromFavorite(@PathVariable Long favoriteRouteId, @RequestBody CreateRideFromFavoriteDTO dto) {

        CreatedRideDTO response = new CreatedRideDTO();
        response.setId(2L);
        response.setStatus(RideStatus.SCHEDULED);
        response.setPrice(1200.0);
        response.setMessage("Ride created from favorite route");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2.6.1
    @PutMapping("/{rideId}/start")
    public ResponseEntity<Void> startRide(@PathVariable Long rideId) {
        rideService.startRide(rideId);
        return ResponseEntity.ok().build();
    }

    //2.6.2
    @GetMapping(value = "/now/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrackingRideDTO> getTrackingRide(@PathVariable String username){
        // find that ride
        RouteDTO route = new RouteDTO();
        route.appendStop(new CoordinateDTO(45.238796, 19.883819));
        route.appendStop(new CoordinateDTO(45.242685, 19.841950));
        route.appendStop(new CoordinateDTO(45.249336, 19.830732));
        route.appendStop(new CoordinateDTO(45.251135, 19.797931));
        LocalDateTime now = LocalDateTime.now();
        TrackingRideDTO ride = new TrackingRideDTO(3L, route, 2L, 1203, "A", "B", now, false);

        return new ResponseEntity<>(ride, HttpStatus.OK);
    }

    @PostMapping(value ="/report", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createReport(@RequestBody CreateReportDTO createReportDTO) {
        rideService.createReport(createReportDTO);

        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/now/user/{id}/position", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CoordinateDTO> getDriverPosition(@PathVariable Long id){
        // find that ride
        CoordinateDTO position = new CoordinateDTO(45.234116, 19.849381);
        return new ResponseEntity<>(position, HttpStatus.OK);
    }

    // 2.7
    @PutMapping(value="/{username}/end", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> endRide(@PathVariable String username) {
        Long rideId = rideService.endRide(username);
        if (rideId == null) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(rideId); // 201
    }

    @GetMapping(value = "/scheduled-ride/{rideId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScheduledRideDTO> getScheduledRide(@PathVariable Long rideId) {
        ScheduledRideDTO ride = rideService.getScheduledRide(rideId);
        return ResponseEntity.ok(ride);
    }



    
    // 2.5 Cancel ride
    @PutMapping(value = "/{rideId}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedRideDTO> cancelRide(@PathVariable Long rideId, @RequestBody CancelRideDTO dto) {

        CreatedRideDTO response = new CreatedRideDTO();
        response.setId(rideId);
        response.setStatus(RideStatus.CANCELED);   
        response.setPrice(0.0);
        response.setMessage("Ride canceled. Reason: " + dto.getReason());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    
    // 2.6.5 Stop ride while it's active
    @PutMapping(value = "/{rideId}/stop", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedRideDTO> stopRide(@PathVariable Long rideId) {

        CreatedRideDTO response = new CreatedRideDTO();
        response.setId(rideId);
        response.setStatus(RideStatus.FINISHED);   
        response.setPrice(900.0);
        response.setMessage("Ride stopped successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/active-ride")
    public ResponseEntity<?> hasActiveRide(@AuthenticationPrincipal User user) {
        boolean hasActive = rideService.userHasActiveRide(user.getId());

        return ResponseEntity.ok(Map.of("hasActiveRide", hasActive));
    }

    // 2.6.3 PANIC
    @PostMapping("/{rideId}/panic")
    public ResponseEntity<Void> panic(@PathVariable Long rideId,
                                      @AuthenticationPrincipal User user) {

        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        rideService.triggerPanic(rideId, user.getId());
        return ResponseEntity.ok().build();
    }








}


