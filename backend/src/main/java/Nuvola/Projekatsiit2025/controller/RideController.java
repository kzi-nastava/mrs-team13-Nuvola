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
}
