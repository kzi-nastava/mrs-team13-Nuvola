package Nuvola.Projekatsiit2025.controller;
import Nuvola.Projekatsiit2025.dto.*;

import Nuvola.Projekatsiit2025.exceptions.ride.RideNotFoundException;
import Nuvola.Projekatsiit2025.model.RegisteredUser;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    public ResponseEntity<TrackingRideDTO> getTrackingRide(@PathVariable String username) {
        // find that ride
//        RouteDTO route = new RouteDTO();
//        route.appendStop(new CoordinateDTO(45.238796, 19.883819));
//        route.appendStop(new CoordinateDTO(45.242685, 19.841950));
//        route.appendStop(new CoordinateDTO(45.249336, 19.830732));
//        route.appendStop(new CoordinateDTO(45.251135, 19.797931));
//        LocalDateTime now = LocalDateTime.now();
//        TrackingRideDTO ride = new TrackingRideDTO(3L, route, 2L, 1203, "A", "B", now, false);
        TrackingRideDTO ride = rideService.getTrackingRideDTO(username);
        return new ResponseEntity<>(ride, HttpStatus.OK);
    }

    @PostMapping(value = "/report", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createReport(@RequestBody CreateReportDTO createReportDTO) {
        rideService.createReport(createReportDTO);

        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/now/user/{id}/position", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CoordinateDTO> getDriverPosition(@PathVariable Long id) {
        // find that ride
        CoordinateDTO position = new CoordinateDTO(45.234116, 19.849381);
        return new ResponseEntity<>(position, HttpStatus.OK);
    }

    // 2.7
    @PutMapping(value = "/{username}/end", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> endRide(@PathVariable String username) {
        Long rideId = null;
        try {
            rideId = rideService.endRide(username);
        } catch (RideNotFoundException e) {
            return ResponseEntity.notFound().build(); // 404
        }
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
    @PatchMapping("/{rideId}/stop")
    public ResponseEntity<CreatedRideDTO> stopRide(
            @PathVariable Long rideId,
            @RequestBody StopRideRequestDTO req,
            Principal principal
    ) {
        User currentUser = userRepository.findByUsername(principal.getName());
        Ride ride = rideService.stopRide(rideId, currentUser, req);

        CreatedRideDTO response = new CreatedRideDTO();
        response.setId(ride.getId());
        response.setStatus(ride.getStatus());
        response.setPrice(ride.getPrice());
        response.setMessage("Ride successfully stopped");

        return ResponseEntity.ok(response);
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

    // 2.9.1 RIDE HISTORY FOR REGISTERED USER
    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getRegisteredUserRideHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "creationTime") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (!(user instanceof RegisteredUser ru)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse("FORBIDDEN", "Only registered users can access ride history"));
        }

        LocalDateTime from = null;
        LocalDateTime to = null;

        try {
            if (fromDate != null && !fromDate.isBlank()) {
                from = LocalDate.parse(fromDate).atStartOfDay();
            }
            if (toDate != null && !toDate.isBlank()) {
                to = LocalDate.parse(toDate).atTime(LocalTime.MAX);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiErrorResponse("BAD_REQUEST", "Invalid date format. Use yyyy-MM-dd"));
        }

        return ResponseEntity.ok(
                rideService.getUserRideHistory(ru.getId(), from, to, sortBy, sortOrder, page, size)
        );
    }

    @GetMapping(value = "/history/{rideId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getRideHistoryDetails(
            @AuthenticationPrincipal User user,
            @PathVariable Long rideId
    ) {
        if (!(user instanceof RegisteredUser ru)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse("FORBIDDEN", "Only registered users can access ride history"));
        }

        return ResponseEntity.ok(
                rideService.getRideHistoryDetailsForUser(rideId, ru.getId())
        );
    }

    @PutMapping("/{rideId}/favorite")
    public ResponseEntity<?> toggleFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable Long rideId
    ) {
        if (!(user instanceof RegisteredUser ru)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse("FORBIDDEN", "Only registered users"));
        }

        boolean newState = rideService.toggleFavoriteRouteForUser(rideId, ru.getId());
        return ResponseEntity.ok(Map.of("favourite", newState));
    }

    // REORDER from History
    // RideController.java
    @PostMapping(value = "/reorder", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> reorderRide(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateRideFromHistoryDTO dto) {

        try {
            Ride ride = rideService.createRideFromHistory(user, dto);

            CreatedRideDTO response = new CreatedRideDTO();
            response.setId(ride.getId());
            response.setStatus(ride.getStatus());
            response.setPrice(ride.getPrice());
            response.setMessage("Ride successfully created from history");

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

}


