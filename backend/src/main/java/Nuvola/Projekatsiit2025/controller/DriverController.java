package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.ActiveVehicleDTO;
import Nuvola.Projekatsiit2025.dto.CreateDriverDTO;
import Nuvola.Projekatsiit2025.dto.CreatedDriverDTO;

import Nuvola.Projekatsiit2025.dto.DriverRideHistoryItemDTO;
import Nuvola.Projekatsiit2025.repositories.DriverRepository;
import Nuvola.Projekatsiit2025.services.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    @Autowired
    private RideService rideService;

    //2.2.3
    @PostMapping
    public ResponseEntity<CreatedDriverDTO> createDriver(@RequestBody CreateDriverDTO driverDTO) {

        CreatedDriverDTO response = new CreatedDriverDTO();
        response.setId(1L);
        response.setEmail(driverDTO.getEmail());
        response.setFirstName(driverDTO.getFirstName());
        response.setLastName(driverDTO.getLastName());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2.9.2
    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping(value = "/{driverId}/rides", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DriverRideHistoryItemDTO>> getDriverRideHistory(
            @PathVariable Long driverId,
            @RequestParam(required = false, defaultValue = "startingTime") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {

        List<DriverRideHistoryItemDTO> rides = rideService.getDriverRideHistory(driverId, sortBy, sortOrder);
        return ResponseEntity.ok(rides);


    }

    // 2.1.1
    @GetMapping(value="/active-vehicles", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ActiveVehicleDTO>> getActiveVehicles() {
        List<ActiveVehicleDTO> response = new ArrayList<>();
        ActiveVehicleDTO activeVehicleDTO = new ActiveVehicleDTO();
        response.add(activeVehicleDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
