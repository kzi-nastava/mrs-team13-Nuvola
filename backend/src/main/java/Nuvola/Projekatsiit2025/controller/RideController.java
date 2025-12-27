package Nuvola.Projekatsiit2025.controller;
import Nuvola.Projekatsiit2025.dto.*;

import Nuvola.Projekatsiit2025.model.RideStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    // 2.4.1
    @PostMapping
    public ResponseEntity<CreatedRideDTO> createRide(@RequestBody CreateRideDTO dto) {

        CreatedRideDTO response = new CreatedRideDTO();
        response.setId(1L);
        response.setStatus(RideStatus.SCHEDULED);
        response.setPrice(1200.0);
        response.setMessage("Ride successfully created");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
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


