package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.*;
import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.Location;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.services.DriverService;
import Nuvola.Projekatsiit2025.repositories.DriverRepository;
import Nuvola.Projekatsiit2025.services.RideService;
import Nuvola.Projekatsiit2025.services.VehicleTrackingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "*")
public class DriverController {
    @Autowired
    private RideService rideService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private DriverRepository driverRepository;

    //2.2.3
    //@PreAuthorize("hasRole('ADMIN')") ovo nakon sto se namesti login kao admin
    @PostMapping
    public ResponseEntity<?> createDriver(@Valid @RequestBody CreateDriverDTO dto) {
        Driver saved = driverService.createDriver(dto);

        CreatedDriverDTO response = new CreatedDriverDTO();
        response.setId(saved.getId());
        response.setEmail(saved.getEmail());
        response.setFirstName(saved.getFirstName());
        response.setLastName(saved.getLastName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/picture")
    public ResponseEntity<?> uploadDriverPicture(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String filename = "profile_" + driver.getId() + ".png";

        Path uploadPath = Paths.get("C:/uploads/profile-pictures/");
        Files.createDirectories(uploadPath);

        Files.write(uploadPath.resolve(filename), file.getBytes());

        driver.setPicture(filename);
        driverRepository.save(driver);

        return ResponseEntity.ok(Map.of("picture", filename));
    }


    // 2.9.2
    //@PreAuthorize("hasRole('DRIVER')")
    @GetMapping(value = "/{username}/rides", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<DriverRideHistoryItemDTO>> getDriverRideHistory(
            @PathVariable String username,
            @RequestParam(required = false, defaultValue = "startTime") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

//        // TEMPORARY: Mock data for testing when database is empty
//        List<DriverRideHistoryItemDTO> mockRides = createMockRides();
//
//        // Sort mock data
//        if (sortOrder.equalsIgnoreCase("desc")) {
//            mockRides.sort((a, b) -> b.getStartingTime().compareTo(a.getStartingTime()));
//        } else {
//            mockRides.sort((a, b) -> a.getStartingTime().compareTo(b.getStartingTime()));
//        }
//
//        // Create page from mock data
//        int pageNumber = (page != null) ? page : 0;
//        int pageSize = (size != null) ? size : mockRides.size();
//
//        Page<DriverRideHistoryItemDTO> mockPage = new PageImpl<>(
//                mockRides,
//                PageRequest.of(pageNumber, pageSize),
//                mockRides.size()
//        );

        Page<DriverRideHistoryItemDTO> rides = rideService.getDriverRideHistory(username, sortBy, sortOrder, page, size);

        return ResponseEntity.ok(rides);

    }

    @GetMapping("/{username}/assigned-rides")
    public ResponseEntity<List<DriverAssignedRideDTO>> getAssignedRides(
            @PathVariable String username) {

        List<Ride> rides = rideService.getAssignedRidesForDriver(username);

        List<DriverAssignedRideDTO> dtos =
                rides.stream()
                        .map(DriverAssignedRideDTO::new)
                        .toList();

        System.out.println("USERNAME FROM URL: " + username);

        return ResponseEntity.ok(dtos);
    }

    private List<DriverRideHistoryItemDTO> createMockRides() {
        List<DriverRideHistoryItemDTO> rides = new ArrayList<>();

        // Ride 1: Novi Sad centar -> Petrovaradin tvrđava
        DriverRideHistoryItemDTO ride1 = new DriverRideHistoryItemDTO();
        ride1.setId(1L);
        ride1.setPrice(450.0);

//        Location pickup1 = new Location();
//        pickup1.setLatitude(45.2671);  // Trg Slobode, Novi Sad
//        pickup1.setLongitude(19.8335);
        ride1.setPickup("Trg Slobode, Novi Sad");

//        Location dropoff1 = new Location();
//        dropoff1.setLatitude(45.2517);  // Petrovaradinska tvrđava
//        dropoff1.setLongitude(19.8659);
        ride1.setDropoff("Petrovaradinska tvrđava, Novi Sad");

        ride1.setStartingTime(LocalDateTime.of(2026, 1, 25, 10, 30));
        ride1.setDriver("Marko Marković");
        ride1.setFavouriteRoute(true);

        rides.add(ride1);

        // Ride 2: Novi Sad železnička stanica -> Futoški park
        DriverRideHistoryItemDTO ride2 = new DriverRideHistoryItemDTO();
        ride2.setId(2L);
        ride2.setPrice(320.0);

//        Location pickup2 = new Location();
//        pickup2.setLatitude(45.2559);  // Železnička stanica
//        pickup2.setLongitude(19.8404);
        ride2.setPickup("Železnička stanica, Novi Sad");

//        Location dropoff2 = new Location();
//        dropoff2.setLatitude(45.2396);  // Futoški park
//        dropoff2.setLongitude(19.8227);
        ride2.setDropoff("Futoški park, Novi Sad");

        ride2.setStartingTime(LocalDateTime.of(2026, 1, 27, 15, 45));
        ride2.setDriver("Ana Anić");
        ride2.setFavouriteRoute(false);

        rides.add(ride2);

        DriverRideHistoryItemDTO ride3 = new DriverRideHistoryItemDTO();
        ride3.setId(3L);
        ride3.setPrice(500.0);
        ride3.setPickup("Bulevar Oslobođenja, Novi Sad");
        ride3.setDropoff("Sajmište, Novi Sad");
        ride3.setStartingTime(LocalDateTime.of(2026, 1, 30, 12, 0));
        ride3.setDriver("Marko Marković");
        ride3.setFavouriteRoute(true);
        rides.add(ride3);

        DriverRideHistoryItemDTO ride4 = new DriverRideHistoryItemDTO();
        ride4.setId(4L);
        ride4.setPrice(600.0);
        ride4.setPickup("Limanski park, Novi Sad");
        ride4.setDropoff("Novi Sad centar, Novi Sad");
        ride4.setStartingTime(LocalDateTime.of(2026, 2, 2, 18, 30));
        ride4.setDriver("Ana Anić");
        ride4.setFavouriteRoute(false);
        rides.add(ride4);

        DriverRideHistoryItemDTO ride5 = new DriverRideHistoryItemDTO();
        ride5.setId(5L);
        ride5.setPrice(550.0);
        ride5.setPickup("Novi Sad centar, Novi Sad");
        ride5.setDropoff("Petrovaradin tvrđava, Novi Sad");
        ride5.setStartingTime(LocalDateTime.of(2026, 2, 5, 9, 15));
        ride5.setDriver("Marko Marković");
        ride5.setFavouriteRoute(false);
        rides.add(ride5);

        return rides;
    }

    // 2.1.1
//    @GetMapping(value="/active-vehicles", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<ActiveVehicleDTO>> getActiveVehicles() {
//        List<ActiveVehicleDTO> response = new ArrayList<>();
//        ActiveVehicleDTO activeVehicleDTO = new ActiveVehicleDTO(45.2671, 19.8335, true, 1L);
//        response.add(activeVehicleDTO);
//        ActiveVehicleDTO activeVehicleDTO2 = new ActiveVehicleDTO(44.7866, 20.4489, false, 2L);
//        response.add(activeVehicleDTO2);
//
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }


//    @PreAuthorize("hasRole('DRIVER')")
//    @PutMapping("/{vehicleId}/location")
//    public ResponseEntity<Void> updateLocation(
//            @PathVariable Long vehicleId,
//            @RequestBody LocationUpdateRequestDTO request) {
//
//        try {
//            vehicleTrackingService.updateVehicleLocation(
//                    new VehiclePositionDTO(
//                            vehicleId,
//                            request.getLatitude(),
//                            request.getLongitude()
//                    )
//            );
//        }  catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        return ResponseEntity.ok().build();
//    }

}
