package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.AdminRideDetailsDTO;
import Nuvola.Projekatsiit2025.dto.AdminRideHistoryItemDTO;
import Nuvola.Projekatsiit2025.dto.AdminUserDTO;
import Nuvola.Projekatsiit2025.dto.BlockUserRequestDTO;
import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.RegisteredUser;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import Nuvola.Projekatsiit2025.repositories.DriverRepository;
import Nuvola.Projekatsiit2025.repositories.RegisteredUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    @Autowired
    private DriverRepository driverRepository;

    // 2.9.3 History of rides (driver/passenger), filter by creationDate and sort 
    @GetMapping(value = "/rides/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AdminRideHistoryItemDTO>> history(
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String from,   // "2025-12-01"
            @RequestParam(required = false) String to,     // "2025-12-27"
            @RequestParam(defaultValue = "creationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        //  data the ,newest for CreationDate
        AdminRideHistoryItemDTO a = new AdminRideHistoryItemDTO();
        a.setId(101L);
        a.setStartLocation("A");
        a.setDestination("B");
        a.setStartTime("2025-12-27T12:30");
        a.setEndTime("2025-12-27T12:55");
        a.setCreationDate("2025-12-27");
        a.setCanceled(false);
        a.setCanceledBy(null);
        a.setPrice(900.0);
        a.setPanic(false);
        a.setStatus(RideStatus.FINISHED);

        AdminRideHistoryItemDTO b = new AdminRideHistoryItemDTO();
        b.setId(100L);
        b.setStartLocation("C");
        b.setDestination("D");
        b.setStartTime("2025-12-20T18:10");
        b.setEndTime("2025-12-20T18:20");
        b.setCreationDate("2025-12-20");
        b.setCanceled(true);
        b.setCanceledBy("PASSENGER");
        b.setPrice(0.0);
        b.setPanic(true);
        b.setStatus(RideStatus.CANCELED);

        List<AdminRideHistoryItemDTO> list = List.of(a, b);

        // sort
        List<AdminRideHistoryItemDTO> sorted = list.stream()
                .sorted(getComparator(sortBy, sortDir))
                .toList();

        return ResponseEntity.ok(sorted);
    }

    // 2.9.3 Ride details
    @GetMapping(value = "/rides/{rideId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminRideDetailsDTO> details(@PathVariable Long rideId) {

        AdminRideDetailsDTO dto = new AdminRideDetailsDTO();
        dto.setId(rideId);

        dto.setStartLocation("A");
        dto.setDestination("B");
        dto.setStartTime("2025-12-27T12:30");
        dto.setEndTime("2025-12-27T12:55");
        dto.setCreationDate("2025-12-27");

        dto.setPrice(900.0);
        dto.setPanic(false);

        // map
        dto.setRouteCoordinates(List.of("45.2671,19.8335", "45.2520,19.8360"));

        // driver/passengers 
        dto.setDriverName("Test Driver");
        dto.setPassengerNames(List.of("Ana Test", "Marko Test"));

        // reports/ratings 
        dto.setInconsistencyReports(List.of("Late arrival reported", "Different route reported"));
        dto.setDriverRating(4.8);
        dto.setPassengersRating(4.6);

        // reorder stub
        dto.setCanReorderNow(true);
        dto.setCanReorderLater(true);

        return ResponseEntity.ok(dto);
    }

    private Comparator<AdminRideHistoryItemDTO> getComparator(String sortBy, String sortDir) {
        Comparator<AdminRideHistoryItemDTO> cmp;

        switch (sortBy) {
            case "price" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::getPrice);
            case "startTime" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::getStartTime);
            case "endTime" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::getEndTime);
            case "startLocation" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::getStartLocation);
            case "destination" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::getDestination);
            case "canceled" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::isCanceled);
            case "panic" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::isPanic);
            case "status" -> cmp = Comparator.comparing(r -> r.getStatus().name());
            case "creationDate" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::getCreationDate);
            default -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::getCreationDate);
        }

        return "desc".equalsIgnoreCase(sortDir) ? cmp.reversed() : cmp;
    }

    // all registered users and drivers

    @GetMapping("users/registeredUsers")
    public ResponseEntity<List<AdminUserDTO>> getAllRegisteredUsers() {

        List<AdminUserDTO> users = registeredUserRepository.findAll()
                .stream()
                .map(u -> new AdminUserDTO(
                        u.getId(),
                        u.getFirstName(),
                        u.getLastName(),
                        u.getEmail(),
                        u.getAddress(),
                        u.getPhone(),
                        u.getPicture(),
                        u.isBlocked(),
                        u.getBlockingReason()
                ))
                .toList();

        return ResponseEntity.ok(users);
    }


    @GetMapping("users/drivers")
    public ResponseEntity<List<AdminUserDTO>> getAllDrivers() {

        List<AdminUserDTO> drivers = driverRepository.findAll()
                .stream()
                .map(d -> new AdminUserDTO(
                        d.getId(),
                        d.getFirstName(),
                        d.getLastName(),
                        d.getEmail(),
                        d.getAddress(),
                        d.getPhone(),
                        d.getPicture(),
                        d.isBlocked(),
                        d.getBlockingReason()
                ))
                .toList();

        return ResponseEntity.ok(drivers);
    }

    @PostMapping("/users/{id}/block")
    public ResponseEntity<AdminUserDTO> blockUser(
            @PathVariable Long id,
            @RequestBody BlockUserRequestDTO request
    ) {

        // prvo pokušamo registered user
        RegisteredUser ru = registeredUserRepository.findById(id).orElse(null);

        if (ru != null) {
            ru.setBlocked(true);

            if (request.getBlockingReason() != null && !request.getBlockingReason().isBlank()) {
                ru.setBlockingReason(request.getBlockingReason());
            } else {
                ru.setBlockingReason(null);
            }

            registeredUserRepository.save(ru);

            return ResponseEntity.ok(new AdminUserDTO(
                    ru.getId(),
                    ru.getFirstName(),
                    ru.getLastName(),
                    ru.getEmail(),
                    ru.getAddress(),
                    ru.getPhone(),
                    ru.getPicture(),
                    ru.isBlocked(),
                    ru.getBlockingReason()
            ));
        }

        // ako nije registered user, pokušamo driver
        Driver driver = driverRepository.findById(id).orElse(null);

        if (driver != null) {
            driver.setBlocked(true);

            if (request.getBlockingReason() != null && !request.getBlockingReason().isBlank()) {
                driver.setBlockingReason(request.getBlockingReason());
            } else {
                driver.setBlockingReason(null);
            }

            driverRepository.save(driver);

            return ResponseEntity.ok(new AdminUserDTO(
                    driver.getId(),
                    driver.getFirstName(),
                    driver.getLastName(),
                    driver.getEmail(),
                    driver.getAddress(),
                    driver.getPhone(),
                    driver.getPicture(),
                    driver.isBlocked(),
                    driver.getBlockingReason()
            ));
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<AdminUserDTO> unblockUser(@PathVariable Long id) {

        RegisteredUser ru = registeredUserRepository.findById(id).orElse(null);

        if (ru != null) {
            ru.setBlocked(false);
            ru.setBlockingReason(null);

            registeredUserRepository.save(ru);

            return ResponseEntity.ok(new AdminUserDTO(
                    ru.getId(),
                    ru.getFirstName(),
                    ru.getLastName(),
                    ru.getEmail(),
                    ru.getAddress(),
                    ru.getPhone(),
                    ru.getPicture(),
                    ru.isBlocked(),
                    ru.getBlockingReason()
            ));
        }

        Driver driver = driverRepository.findById(id).orElse(null);

        if (driver != null) {
            driver.setBlocked(false);
            driver.setBlockingReason(null);

            driverRepository.save(driver);

            return ResponseEntity.ok(new AdminUserDTO(
                    driver.getId(),
                    driver.getFirstName(),
                    driver.getLastName(),
                    driver.getEmail(),
                    driver.getAddress(),
                    driver.getPhone(),
                    driver.getPicture(),
                    driver.isBlocked(),
                    driver.getBlockingReason()
            ));
        }

        return ResponseEntity.notFound().build();
    }



}
