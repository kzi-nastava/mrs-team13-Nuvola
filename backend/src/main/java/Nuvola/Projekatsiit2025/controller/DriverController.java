package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.ActiveVehicleDTO;
import Nuvola.Projekatsiit2025.dto.CreateDriverDTO;
import Nuvola.Projekatsiit2025.dto.CreatedDriverDTO;

import Nuvola.Projekatsiit2025.dto.DriverRideHistoryItemDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

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
    @GetMapping(value = "/{driverId}/rides", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DriverRideHistoryItemDTO>> getDriverRideHistory(
            @PathVariable Long driverId,
            @RequestParam(required = false, defaultValue = "startingTime") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. hh:mm a");
        List<DriverRideHistoryItemDTO> response = new ArrayList<>();
        DriverRideHistoryItemDTO ride1 = new DriverRideHistoryItemDTO();
        ride1.setId(1L);
        ride1.setDriver("John Smith");
        ride1.setPickup("Location A");
        ride1.setDropoff("Location B");
        ride1.setPrice(15);
        ride1.setStartingTime(LocalDateTime.parse("12.01.2026. 11:00 AM" ,formatter));
        ride1.setFavouriteRoute(false);

        DriverRideHistoryItemDTO ride2 = new DriverRideHistoryItemDTO();
        ride2.setId(2L);
        ride2.setDriver("John Smith");
        ride2.setPickup("Location C");
        ride2.setDropoff("Location D");
        ride2.setPrice(38);
        ride2.setStartingTime(LocalDateTime.parse("13.01.2026. 10:00 AM",formatter));
        ride2.setFavouriteRoute(false);

        response.add(ride1);
        response.add(ride2);
        return new ResponseEntity<>(response, HttpStatus.OK);

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
