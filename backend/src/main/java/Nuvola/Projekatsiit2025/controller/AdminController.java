package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.*;
import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.ProfileChangeRequest;
import Nuvola.Projekatsiit2025.model.RegisteredUser;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.enums.RequestStatus;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import Nuvola.Projekatsiit2025.repositories.DriverRepository;
import Nuvola.Projekatsiit2025.repositories.ProfileChangeRequestRepository;
import Nuvola.Projekatsiit2025.repositories.RegisteredUserRepository;
import Nuvola.Projekatsiit2025.repositories.RideRepository;
import Nuvola.Projekatsiit2025.services.RideService;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RideService rideService;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private ProfileChangeRequestRepository requestRepository;
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

    @GetMapping(value = "/panic", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PanicDTO>> getPanicNotifications() {
        return ResponseEntity.ok(rideService.getActivePanicNotifications());
    }

    @GetMapping("/profile-change-requests")
    public ResponseEntity<List<ProfileChangeRequestDTO>> getPendingRequests() {
        List<ProfileChangeRequest> requests = requestRepository
                .findByStatus(RequestStatus.PENDING);

        List<ProfileChangeRequestDTO> dtos = requests.stream()
                .map(this::mapToDTO)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/profile-change-requests/{id}/approve")
    public ResponseEntity<Void> approveRequest(@PathVariable Long id) {
        ProfileChangeRequest req = requestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!(req.getDriver() instanceof Driver)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid driver");
        }

        Driver driver = (Driver) req.getDriver();

        // Update driver data
        driver.setFirstName(req.getFirstName());
        driver.setLastName(req.getLastName());
        driver.setPhone(req.getPhone());
        driver.setAddress(req.getAddress());

        // Update vehicle
        driver.getVehicle().setModel(req.getModel());
        driver.getVehicle().setType(req.getType());
        driver.getVehicle().setNumOfSeats(req.getNumOfSeats());
        driver.getVehicle().setBabyFriendly(req.getBabyFriendly());
        driver.getVehicle().setPetFriendly(req.getPetFriendly());

        driverRepository.save(driver);

        // Mark request as approved
        req.setStatus(RequestStatus.APPROVED);
        requestRepository.save(req);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/profile-change-requests/{id}/reject")
    public ResponseEntity<Void> rejectRequest(@PathVariable Long id) {
        ProfileChangeRequest req = requestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        req.setStatus(RequestStatus.REJECTED);
        requestRepository.save(req);

        return ResponseEntity.ok().build();
    }

    private ProfileChangeRequestDTO mapToDTO(ProfileChangeRequest req) {
        ProfileChangeRequestDTO dto = new ProfileChangeRequestDTO();
        if (!(req.getDriver() instanceof Driver)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid driver");
        }

        Driver driver = (Driver) req.getDriver();

        dto.setId(req.getId());
        dto.setDriverName(driver.getFirstName() + " " + driver.getLastName());
        dto.setDriverEmail(driver.getEmail());

        // CURRENT VALUES (from driver)
        dto.setCurrentFirstName(driver.getFirstName());
        dto.setCurrentLastName(driver.getLastName());
        dto.setCurrentPhone(driver.getPhone());
        dto.setCurrentAddress(driver.getAddress());
        dto.setCurrentModel(driver.getVehicle().getModel());
        dto.setCurrentType(driver.getVehicle().getType().toString());
        dto.setCurrentNumOfSeats(driver.getVehicle().getNumOfSeats());
        dto.setCurrentBabyFriendly(driver.getVehicle().isBabyFriendly());
        dto.setCurrentPetFriendly(driver.getVehicle().isPetFriendly());

        // REQUESTED VALUES (from change request)
        dto.setFirstName(req.getFirstName());
        dto.setLastName(req.getLastName());
        dto.setPhone(req.getPhone());
        dto.setAddress(req.getAddress());
        dto.setModel(req.getModel());
        dto.setType(req.getType().toString());
        dto.setNumOfSeats(req.getNumOfSeats());
        dto.setBabyFriendly(req.getBabyFriendly());
        dto.setPetFriendly(req.getPetFriendly());

        dto.setStatus(req.getStatus().toString());
        dto.setCreatedAt(req.getCreatedAt().toString());

        return dto;
    }

    /**
     * Pretraži voznje po vozaču ili putniku sa paginacijom
     * GET /api/admin/rides/history?driverId=1&page=0&size=10&sortBy=creationTime&sortDir=desc
     * GET /api/admin/rides/history?userId=5&from=2025-12-01&to=2025-12-31
     */
    @GetMapping(value = "/rides/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<AdminRideHistoryItemDTO>> history(
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "creationTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir.trim().toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        // Konvertuj LocalDate u LocalDateTime
        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;

        if (from != null) {
            fromDateTime = from.atStartOfDay();
        }
        if (to != null) {
            toDateTime = to.atTime(23, 59, 59);
        }

        // Kreiraj Sort objekat

        Sort sort = Sort.by(direction, sortBy);


        Page<Ride> rides;

        // ===== FILTRIRANJE =====
        if (driverId != null) {
            // Voznje određenog vozača
            if (fromDateTime != null && toDateTime != null) {
                rides = rideRepository.findByDriver_IdAndCreationTimeBetween(
                        driverId, fromDateTime, toDateTime, pageable
                );
            } else {
                rides = rideRepository.findByDriver_Id(driverId, pageable);
            }
        } else if (userId != null) {
            // Voznje određenog putnika (creator i other passengers)
            Page<Ride> creatorRides;
            Page<Ride> passengerRides;

            if (fromDateTime != null && toDateTime != null) {
                creatorRides = rideRepository.findByCreator_IdAndCreationTimeBetween(
                        userId, fromDateTime, toDateTime, pageable
                );
                passengerRides = rideRepository.findByOtherPassenger_Id(userId, pageable);
            } else {
                creatorRides = rideRepository.findByCreator_Id(userId, pageable);
                passengerRides = rideRepository.findByOtherPassenger_Id(userId, pageable);
            }

            // Kombinuj oba
            List<Ride> combined = creatorRides.getContent();
            combined.addAll(passengerRides.getContent());
            rides = new org.springframework.data.domain.PageImpl<>(
                    combined.stream().distinct().collect(Collectors.toList()),
                    pageable,
                    combined.stream().distinct().count()
            );
        } else {
            // Sve voznje
            if (fromDateTime != null && toDateTime != null) {
                rides = rideRepository.findByCreationTimeBetween(fromDateTime, toDateTime, pageable);
            } else {
                rides = rideRepository.findAll(pageable);
            }
        }

        return ResponseEntity.ok(
                rides.map(this::mapToAdminRideHistoryDTO)
        );
    }

    /**
     * Detaljni prikaz voznje
     * GET /api/admin/rides/1
     */
    @GetMapping(value = "/rides/{rideId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminRideDetailsDTO> details(@PathVariable Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Voznja sa ID " + rideId + " nije pronađena"
                ));
        AdminRideDetailsDTO dto = mapToAdminRideDetailsDTO(ride);
        return ResponseEntity.ok(dto);
    }

    /**
     * Sve voznje sa PANIC aktiviranim
     */
    @GetMapping(value = "/rides/panic/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<AdminRideHistoryItemDTO>> panicRides(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creationTime"));
        Page<Ride> rides = rideRepository.findByIsPanicTrue(pageable);

        return ResponseEntity.ok(
                rides.map(this::mapToAdminRideHistoryDTO)
        );
    }

    /**
     * Sve otkazane voznje
     */
    //@GetMapping(value = "/rides/cancelled/all", produces = MediaType.APPLICATION_JSON_VALUE)
    //public ResponseEntity<Page<AdminRideHistoryItemDTO>> cancelledRides(
        //    @RequestParam(defaultValue = "0") int page,
      //      @RequestParam(defaultValue = "10") int size
    //) {
       // Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creationTime"));


        //return ResponseEntity.ok(
        //        rides.map(this::mapToAdminRideHistoryDTO)
      //  );
    //}

    // ============== MAPPERS ==============

    /**
     * Mapira Ride na AdminRideHistoryItemDTO za listu
     */
    private AdminRideHistoryItemDTO mapToAdminRideHistoryDTO(Ride ride) {
        AdminRideHistoryItemDTO dto = new AdminRideHistoryItemDTO();

        dto.setId(ride.getId());
        if (ride.getRoute() != null) {
            if (ride.getRoute().getPickup() != null) {
                dto.setStartLocation(ride.getRoute().getPickup().getAddress());
            }
            if (ride.getRoute().getDropoff() != null) {
                dto.setDestination(ride.getRoute().getDropoff().getAddress());
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        if (ride.getStartTime() != null) {
            dto.setStartTime(ride.getStartTime().format(formatter));
        } else {
            dto.setStartTime("N/A");
        }
        if (ride.getEndTime() != null) {
            dto.setEndTime(ride.getEndTime().format(formatter));
        } else {
            dto.setEndTime("N/A");
        }
        if (ride.getCreationTime() != null) {
            dto.setCreationDate(ride.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        dto.setCanceled(ride.getStatus().name().equals("CANCELED"));
        dto.setCanceledBy(null);
        dto.setPrice(ride.getPrice());
        dto.setPanic(ride.isPanic());
        dto.setStatus(ride.getStatus());
        return dto;
    }

    /**
     * Mapira Ride na AdminRideDetailsDTO sa detaljima
     */
    /**
     * Mapira Ride na AdminRideDetailsDTO sa svim detaljima
     */
    private AdminRideDetailsDTO mapToAdminRideDetailsDTO(Ride ride) {
        AdminRideDetailsDTO dto = new AdminRideDetailsDTO();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // ========== OSNOVNI PODACI ==========
        dto.setId(ride.getId());
        dto.setPrice(ride.getPrice());
        dto.setPanic(ride.isPanic());
        //dto.set(ride.getStatus());

        // ========== LOKACIJE ==========
        //if (ride.getRoute() != null) {
          //  if (ride.getRoute().getPickup() != null) {
            //    dto.setStartLocation(ride.getRoute().getPickup().getAddress());
            //}
           // if (ride.getRoute().getDropoff() != null) {
             //   dto.setDestination(ride.getRoute().getDropoff().getAddress());
            //}
        //}


        // ========== LOKACIJE ==========
        if (ride.getRoute() != null) {
            if (ride.getRoute().getPickup() != null) {
                dto.setStartLocation(ride.getRoute().getPickup().getAddress());
            }
            if (ride.getRoute().getDropoff() != null) {
                dto.setDestination(ride.getRoute().getDropoff().getAddress());
            }

            // 🗺️ ROUTE KOORDINATE (pickup -> stops -> dropoff)
            List<String> coordinates = new java.util.ArrayList<>();

            // Dodaj pickup
            if (ride.getRoute().getPickup() != null) {
                String pickupCoord = ride.getRoute().getPickup().getLatitude() + "," +
                        ride.getRoute().getPickup().getLongitude();
                coordinates.add(pickupCoord);
            }

            // Dodaj sve intermediate stops
            if (ride.getRoute().getStops() != null && !ride.getRoute().getStops().isEmpty()) {
                ride.getRoute().getStops().forEach(stop -> {
                    if (stop.getLatitude() != 0 && stop.getLongitude() != 0) {
                        String stopCoord = stop.getLatitude() + "," + stop.getLongitude();
                        coordinates.add(stopCoord);
                    }
                });
            }

            // Dodaj dropoff
            if (ride.getRoute().getDropoff() != null) {
                String dropoffCoord = ride.getRoute().getDropoff().getLatitude() + "," +
                        ride.getRoute().getDropoff().getLongitude();
                coordinates.add(dropoffCoord);
            }

            // Postavi ako ima koordinata
            if (!coordinates.isEmpty()) {
                dto.setRouteCoordinates(coordinates);
            }
        }





        // ========== VREMENSKE OZNAKE ==========
        if (ride.getStartTime() != null) {
            dto.setStartTime(ride.getStartTime().format(formatter));
        } else {
            dto.setStartTime("N/A");
        }

        if (ride.getEndTime() != null) {
            dto.setEndTime(ride.getEndTime().format(formatter));
        } else {
            dto.setEndTime("N/A");
        }

        if (ride.getCreationTime() != null) {
            dto.setCreationDate(ride.getCreationTime().format(dateFormatter));
        }

        // ========== VOZAČ ==========
        if (ride.getDriver() != null) {
            String driverName = ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName();
            dto.setDriverName(driverName);
        }

        // ========== PUTNICI ==========
        List<String> passengerNames = new java.util.ArrayList<>();

        // Dodaj kreatora (prvi putnik)
        if (ride.getCreator() != null) {
            passengerNames.add(ride.getCreator().getFirstName() + " " + ride.getCreator().getLastName());
        }

        // Dodaj ostale putnike
        if (ride.getOtherPassengers() != null && !ride.getOtherPassengers().isEmpty()) {
            ride.getOtherPassengers().forEach(p ->
                    passengerNames.add(p.getFirstName() + " " + p.getLastName())
            );
        }

        dto.setPassengerNames(passengerNames);

        // ========== PRIJAVE O NEKONZISTENTNOSTI ==========
        if (ride.getReports() != null && !ride.getReports().isEmpty()) {
            List<String> reportsList = ride.getReports().stream()
                    .map(r -> r.getReason())
                    .collect(Collectors.toList());
            dto.setInconsistencyReports(reportsList);
        } else {
            dto.setInconsistencyReports(new java.util.ArrayList<>());
        }

        // ========== OCJENE ==========
        if (ride.getRatings() != null && !ride.getRatings().isEmpty()) {
            // Prosječna ocjena vozača
            double avgDriverRating = ride.getRatings().stream()
                    .filter(r -> r.getDriverRating() != null)
                    .mapToInt(r -> r.getDriverRating())
                    .average()
                    .orElse(0.0);
            dto.setDriverRating(avgDriverRating);

            // Prosječna ocjena putnika (vehicle rating)
            double avgPassengerRating = ride.getRatings().stream()
                    .filter(r -> r.getVehicleRating() != null)
                    .mapToInt(r -> r.getVehicleRating())
                    .average()
                    .orElse(0.0);
            dto.setPassengersRating(avgPassengerRating);
        }

        // ========== OPCIJE ZA PONOVNO SLANJE ==========
        boolean isCompleted = ride.getStatus() == RideStatus.FINISHED;
        boolean isCanceled = ride.getStatus() == RideStatus.CANCELED;

        dto.setCanReorderNow(isCompleted || isCanceled);
        dto.setCanReorderLater(isCompleted || isCanceled);

        return dto;
    }

}
