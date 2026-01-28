package Nuvola.Projekatsiit2025.controller;
import Nuvola.Projekatsiit2025.dto.*;

import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import Nuvola.Projekatsiit2025.services.EmailService;
import Nuvola.Projekatsiit2025.services.RideService;
import Nuvola.Projekatsiit2025.util.EmailDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    @Autowired
    EmailService emailService;

    @Autowired
    private RideService rideService;


    // 2.4.1
    @PostMapping
    public ResponseEntity<CreatedRideDTO> createRide(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateRideDTO dto) {

        Ride ride = rideService.createRide(user, dto);

        CreatedRideDTO response = new CreatedRideDTO();
        response.setId(ride.getId());
        response.setStatus(ride.getStatus());
        response.setPrice(ride.getPrice());
        response.setMessage("Ride successfully created");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

        return ResponseEntity.ok().build();
    }

    //2.6.2
    @GetMapping(value = "/now/user/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrackingRideDTO> getTrackingRide(@PathVariable("id") Long id){
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

    @GetMapping(value = "/now/user/{id}/position", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CoordinateDTO> getDriverPosition(@PathVariable("id") Long id){
        // find that ride
        CoordinateDTO position = new CoordinateDTO(45.234116, 19.849381);
        return new ResponseEntity<>(position, HttpStatus.OK);
    }

    // 2.7
    @PutMapping(value="/{rideId}/end", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ScheduledRideDTO>> endRide(@PathVariable Long rideId) {
        List<ScheduledRideDTO> rides = new ArrayList<>();
        // via service find scheduled ride for this driver and for current time

        return new ResponseEntity<List<ScheduledRideDTO>>(rides, HttpStatus.OK);
    }



    // 2.1.2
    @PostMapping(value = "/estimate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RideEstimateResponseDTO> estimateRide(@RequestBody RideEstimateRequestDTO dto) {

        RideEstimateResponseDTO response = new RideEstimateResponseDTO(
                dto.getStartAddress(),
                dto.getDestinationAddress(),
                12 // stub procena
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
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



}


