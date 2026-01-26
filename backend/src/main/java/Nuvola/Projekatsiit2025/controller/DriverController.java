package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.*;

import Nuvola.Projekatsiit2025.services.RideService;
import Nuvola.Projekatsiit2025.services.VehicleTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    @Autowired
    private RideService rideService;

    @Autowired
    private VehicleTrackingService vehicleTrackingService;

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
    public ResponseEntity<Page<DriverRideHistoryItemDTO>> getDriverRideHistory(
            @PathVariable Long driverId,
            @RequestParam(required = false, defaultValue = "startingTime") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        Page<DriverRideHistoryItemDTO> rides = rideService.getDriverRideHistory(
                driverId, sortBy, sortOrder, page, size
        );
        return ResponseEntity.ok(rides);
    }

    // 2.1.1
    @GetMapping(value="/active-vehicles", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VehicleLocationDTO>> getActiveVehicles() {
        List<VehicleLocationDTO> vehicles = vehicleTrackingService.getAllActiveVehicleLocations();
        return ResponseEntity.ok(vehicles);
    }

    @PreAuthorize("hasRole('DRIVER')")
    @PutMapping("/{vehicleId}/location")
    public ResponseEntity<Void> updateLocation(
            @PathVariable Long vehicleId,
            @RequestBody LocationUpdateRequestDTO request) {

        try {
            vehicleTrackingService.updateVehicleLocation(
                    new VehiclePositionDTO(
                            vehicleId,
                            request.getLatitude(),
                            request.getLongitude()
                    )
            );
        }  catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

}
