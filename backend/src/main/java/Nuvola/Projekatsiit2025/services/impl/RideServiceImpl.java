package Nuvola.Projekatsiit2025.services.impl;
import Nuvola.Projekatsiit2025.model.enums.NotificationType;
import Nuvola.Projekatsiit2025.services.NotificationService;
import Nuvola.Projekatsiit2025.services.PricingService;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import Nuvola.Projekatsiit2025.dto.*;
import Nuvola.Projekatsiit2025.exceptions.UserNotFoundException;
import Nuvola.Projekatsiit2025.exceptions.ride.InvalidRideStateException;
import Nuvola.Projekatsiit2025.exceptions.ride.RideNotFoundException;
import Nuvola.Projekatsiit2025.model.*;
import Nuvola.Projekatsiit2025.model.enums.DriverStatus;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import Nuvola.Projekatsiit2025.model.enums.VehicleType;
import Nuvola.Projekatsiit2025.repositories.*;
import Nuvola.Projekatsiit2025.services.EmailService;
import Nuvola.Projekatsiit2025.services.RideService;
import Nuvola.Projekatsiit2025.util.EmailDetails;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RideServiceImpl implements RideService {
    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private PricingService pricingService;

    @Autowired
    EmailService emailService;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationService notificationService;


    @Override
    public Page<DriverRideHistoryItemDTO> getDriverRideHistory(String username, String sortBy, String sortOrder, Integer page, Integer size) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        User driver = userRepository.findByUsername(username);
        Long driverId = driver.getId();

        // if pagination
        if ((page != null && size != null) && ((page >= 1) && (size >= 1))) {
            Pageable pageable = PageRequest.of(page, size, sort);
            // Page<Ride> ridesPage = rideRepository.findByDriverId(driverId, pageable);
            Page<Ride> ridesPage = rideRepository.findByDriverIdAndStatus(driverId, RideStatus.FINISHED, pageable);

            return ridesPage.map(DriverRideHistoryItemDTO::new);
        }

        // List<Ride> rides = rideRepository.findByDriverId(driverId, sort);
        List<Ride> rides = rideRepository.findByDriverIdAndStatus(driverId, RideStatus.FINISHED, sort);
        List<DriverRideHistoryItemDTO> dtos = rides.stream()
                .map(DriverRideHistoryItemDTO::new)
                .collect(Collectors.toList());

        int pageSize = dtos.isEmpty() ? 1 : dtos.size();
        return new PageImpl<>(dtos, PageRequest.of(1, pageSize, sort), dtos.size());
    }

    @Override
    public void startRide(Long rideId) throws RuntimeException {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + rideId));

        Driver driver = ride.getDriver();
        driver.setStatus(DriverStatus.BUSY);
        driverRepository.save(driver);
        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartTime(java.time.LocalDateTime.now());
        rideRepository.saveAndFlush(ride);

        // Send email notification to passengers
        EmailDetails emailDetails = new EmailDetails();
        String baseLink = "http://localhost:4200/ride-tracking/";
        emailDetails.setSubject("Track your ride");
        for (RegisteredUser ru : ride.getOtherPassengers()) {
            emailDetails.setRecipient(ru.getEmail());

            emailDetails.setLink(baseLink + ru.getEmail());
            emailService.sendTrackingPage(emailDetails);
        }
        emailDetails.setLink(baseLink + ride.getCreator().getId());
        emailDetails.setRecipient(ride.getCreator().getEmail());
        emailService.sendTrackingPage(emailDetails);

    }

    private Route createRoute(CreateRideDTO dto) {
        Route route = new Route();

        Location pickup = new Location();
        pickup.setLatitude(dto.getFrom().getLatitude());
        pickup.setLongitude(dto.getFrom().getLongitude());
        pickup.setAddress(dto.getFrom().getAddress());

        Location dropoff = new Location();
        dropoff.setLatitude(dto.getTo().getLatitude());
        dropoff.setLongitude(dto.getTo().getLongitude());
        dropoff.setAddress(dto.getTo().getAddress());

        route.setPickup(pickup);
        route.setDropoff(dropoff);

        List<Location> stopLocations = dto.getStops() == null
                ? List.of()
                : dto.getStops().stream()
                .map(s -> {
                    Location l = new Location();
                    l.setLatitude(s.getLatitude());
                    l.setLongitude(s.getLongitude());
                    l.setAddress(s.getAddress());
                    return l;
                }).toList();
        route.setStops(stopLocations);
        // route.setFavourite(false); nema vise ovog atributa

        return route;
    }

    private Driver findDriver(CreateRideDTO dto) {
        List<Driver> drivers = driverRepository.findActiveDriversWithVehicle();

        return drivers.stream()
                .filter(d -> !d.isBlocked())
                .filter(d -> d.getVehicle().getType() == dto.getVehicleType())
                .filter(d -> !dto.isBabyTransport() || d.getVehicle().isBabyFriendly())
                .filter(d -> !dto.isPetTransport() || d.getVehicle().isPetFriendly())
                .findFirst()
                .orElse(null);
    }

    public Ride createRide(User loggedUser, CreateRideDTO dto) {

        if (loggedUser instanceof RegisteredUser registeredUser) {
            if (registeredUser.isBlocked()) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        registeredUser.getBlockingReason() != null
                                ? "ACCOUNT_BLOCKED: " + registeredUser.getBlockingReason()
                                : "ACCOUNT_BLOCKED"
                );
            }
        }

        Long userId = loggedUser.getId();

        List<Driver> allNonInactiveDrivers = driverRepository.findActiveAndBusyDriversWithVehicle()
                .stream()
                .filter(d -> !d.isBlocked())
                .toList();

        if (allNonInactiveDrivers.isEmpty()) {
            notificationService.sendNotification(
                    userId,
                    "No Active Drivers",
                    "There are currently no active drivers available. Please try again later.",
                    NotificationType.NoVehicleAvailable
            );
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NO_ACTIVE_DRIVERS");
        }

        Route route = createRoute(dto);
        Driver driver = findDriver(dto);

        if (driver == null) {
            notificationService.sendNotification(
                    userId,
                    "No Available Drivers",
                    "All drivers are currently busy. Please try again in a few minutes.",
                    NotificationType.NoVehicleAvailable
            );
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NO_AVAILABLE_DRIVER");
        }

        route = routeRepository.save(route);

        Ride ride = new Ride();
        ride.setStatus(RideStatus.SCHEDULED);
        ride.setCreationTime(LocalDateTime.now());
        ride.setStartTime(dto.getScheduledTime());
        ride.setRoute(route);
        ride.setCreator((RegisteredUser) loggedUser);
        ride.setDriver(driver);

        double price = calculatePrice(10.0, dto.getVehicleType());
        ride.setPrice(price);

        List<RegisteredUser> passengers =
                userRepository.findByEmailIn(dto.getPassengerEmails());
        ride.setOtherPassengers(passengers);

        Ride savedRide = rideRepository.save(ride);

        LocalDateTime scheduledTime = dto.getScheduledTime();
        String rideTimeInfo = scheduledTime != null
                ? " Your ride is scheduled for " + scheduledTime + "."
                : " Your driver is on the way.";

        notificationService.sendNotification(
                userId,
                "Ride Approved",
                "Your ride has been successfully booked." + rideTimeInfo,
                NotificationType.RideApproved
        );

//    if (passengers != null && !passengers.isEmpty()) {
//        for (RegisteredUser passenger : passengers) {
//            notificationService.sendNotification(
//                    passenger.getId(),
//                    "You Have Been Added to a Ride",
//                    "You have been added as a passenger to a ride." + rideTimeInfo,
//                    NotificationType.LinkedPassanger
//            );
//        }
//    }

        notificationService.sendNotification(
                driver.getId(),
                "New Ride Assigned",
                "You have been assigned a new ride. Please check the details and proceed to the pickup location.",
                NotificationType.YouAreAssignedToRide
        );

        return savedRide;
    }

    @Override
    public Long endRide(String username) {
        // find current ride of this driver, if not found throw RideNotFoundException
        // return this driver's scheduled ride
        List<Ride> rides = rideRepository.findByDriver_UsernameAndStatus(username, RideStatus.IN_PROGRESS);
        if (rides.isEmpty()) throw new RideNotFoundException("Ride of " + username + " not found");
        if (rides.size() > 1) throw new InvalidRideStateException(username + " has multiple rides in progress");

        Ride ride = rides.get(0);
        ride.setStatus(RideStatus.FINISHED);
        ride.setEndTime(LocalDateTime.now());

        Driver driver = ride.getDriver();
        driver.setStatus(DriverStatus.ACTIVE);

        rideRepository.save(ride);
        driverRepository.save(driver);

        // send email to passengers
        String messageBody = "Ride ID: " + ride.getId() + "\n" + "Price: " + ride.getPrice() + " RSD\n";
        EmailDetails emailDetails = new EmailDetails("", messageBody, "Ride Ended");
        for (RegisteredUser u : ride.getOtherPassengers()) {
            emailDetails.setRecipient(u.getEmail());
            emailService.sendRideFinished(emailDetails);
            notificationService.sendNotification(u.getId(), "Ride " + ride.getId() + " Ended", "Your ride has ended. Price: " + ride.getPrice() + " RSD", NotificationType.RideEnded);
        }
        emailDetails.setRecipient(ride.getCreator().getEmail());
        emailService.sendRideFinished(emailDetails);
        notificationService.sendNotification(ride.getCreator().getId(), "Ride " + ride.getId() + " Ended", "Your ride has ended. Price: " + ride.getPrice() + " RSD", NotificationType.RideEnded);

        Ride scheduledRide = getNearestScheduledRideForDriver(driver.getId());
        if (scheduledRide == null) {
            return null;
        }

        return scheduledRide.getId();
    }

    @Override
    public void createReport(CreateReportDTO createReportDTO) {
        Report report = new Report();
        RegisteredUser author = registeredUserRepository.findByUsername(createReportDTO.getAuthorUsername());
        if (author == null)
            throw new UserNotFoundException("Registered user " + createReportDTO.getAuthorUsername() + " not found");
        Ride ride = rideRepository.findById(createReportDTO.getRideId()).orElse(null);
        if (ride == null) throw new RideNotFoundException("Ride " + createReportDTO.getRideId() + " not found");

        report.setRide(ride);
        report.setAuthor(author);
        report.setReason(createReportDTO.getReason());
        reportRepository.save(report);

    }

    @Override
    public ScheduledRideDTO getScheduledRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) throw new RideNotFoundException("Ride " + rideId + " not found");
        return new ScheduledRideDTO(ride);
    }

    private Ride getNearestScheduledRideForDriver(Long driverId) {
        return rideRepository
                .findFirstByDriverIdAndStatusAndStartTimeIsNotNullAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
                        driverId,
                        RideStatus.SCHEDULED,
                        LocalDateTime.now()
                )
                .orElse(null);
    }


    private double calculatePrice(double distanceKm, VehicleType type) {
//        double basePrice;
//
//        switch (type) {
//            case STANDARD -> basePrice = 250;
//            case LUXURY -> basePrice = 450;
//            case VAN -> basePrice = 350;
//            default -> basePrice = 250;
//        }

        Double base = pricingService.getOne(type).getBasePrice();
        double vehiclePrice = base != null ? base : 0.0;

        return vehiclePrice + distanceKm * 120;
    }

    public List<Ride> getAssignedRidesForDriver(String username) {

        return rideRepository.findByDriver_UsernameAndStatusIn(
                username,
                List.of(RideStatus.SCHEDULED, RideStatus.IN_PROGRESS)
        );
    }

    @Override
    public boolean userHasActiveRide(Long userId) {
        List<Ride> activeRides = rideRepository.findActiveRidesByUser(userId);

        return !activeRides.isEmpty();
    }

    @Transactional
    public void triggerPanic(Long rideId, Long userId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new RuntimeException("Panic allowed only for IN_PROGRESS rides");
        }

        boolean isDriver = ride.getDriver() != null && ride.getDriver().getId().equals(userId);
        boolean isCreator = ride.getCreator() != null && ride.getCreator().getId().equals(userId);
        boolean isOtherPassenger = ride.getOtherPassengers() != null &&
                ride.getOtherPassengers().stream().anyMatch(p -> p.getId().equals(userId));

        if (!isDriver && !isCreator && !isOtherPassenger) {
            throw new RuntimeException("User not allowed to trigger panic for this ride");
        }

        if (ride.isPanic()) { // već aktiviran
            return;
        }

        ride.setPanic(true);
        rideRepository.save(ride);

        Long driverId = (ride.getDriver() != null) ? ride.getDriver().getId() : null;
        Long creatorId = (ride.getCreator() != null) ? ride.getCreator().getId() : null;

        messagingTemplate.convertAndSend("/topic/admin/panic",
                new PanicDTO(ride.getId(), driverId, creatorId));
    }

    @Override
    public List<PanicDTO> getActivePanicNotifications() {
        return rideRepository.findByIsPanicTrue().stream()
                .map(r -> new PanicDTO(
                        r.getId(),
                        r.getDriver() != null ? r.getDriver().getId() : null,
                        r.getCreator() != null ? r.getCreator().getId() : null
                ))
                .toList();
    }

    @Override
    @Transactional
    public RideHistoryDetailsDTO getRideHistoryDetailsForUser(Long rideId, Long userId) {
//        Ride ride = rideRepository.findRideDetailsForUser(rideId, userId)
//                .orElseThrow(() ->
//                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
//
//        return new RideHistoryDetailsDTO(ride);
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));

        boolean isCreator = ride.getCreator().getId().equals(userId);
        boolean isPassenger = ride.getOtherPassengers().stream()
                .anyMatch(p -> p.getId().equals(userId));

        if (!isCreator && !isPassenger) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found");
        }

        RideHistoryDetailsDTO dto = new RideHistoryDetailsDTO(ride);
        List<Rating> ratings = ratingRepository.findByRideId(rideId);
        dto.setRatings(ratings.stream().map(RatingInfoDTO::new).toList());

        return dto;
    }

    @Override
    public Page<RegisteredUserRideHistoryItemDTO> getUserRideHistory(
            Long userId,
            LocalDateTime from,
            LocalDateTime to,
            String sortBy,
            String sortOrder,
            Integer page,
            Integer size
    ) {
        int pageNumber = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 20;

        Sort sort = (sortOrder != null && sortOrder.equalsIgnoreCase("asc"))
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);


        var spec = (org.springframework.data.jpa.domain.Specification<Ride>) (root, query, cb) -> {
            query.distinct(true);

            var creatorPredicate = cb.equal(root.get("creator").get("id"), userId);


            var passengersJoin = root.join("otherPassengers", jakarta.persistence.criteria.JoinType.LEFT);
            var passengerPredicate = cb.equal(passengersJoin.get("id"), userId);

            var userPredicate = cb.or(creatorPredicate, passengerPredicate);

            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(userPredicate);

            var statusPredicate = cb.equal(root.get("status"), RideStatus.FINISHED);
            predicates.add(statusPredicate);

            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("creationTime"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("creationTime"), to));

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        RegisteredUser ru = registeredUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return rideRepository.findAll(spec, pageable)
                .map(ride -> {
                    RegisteredUserRideHistoryItemDTO dto = new RegisteredUserRideHistoryItemDTO(ride);

                    Long routeId = (ride.getRoute() != null) ? ride.getRoute().getId() : null;

                    boolean fav = routeId != null && ru.getFavoriteRoutes().stream()
                            .anyMatch(r -> r.getId() != null && r.getId().equals(routeId));

                    dto.setFavourite(fav);
                    return dto;
                });

    }

    @Override
    @Transactional
    public boolean toggleFavoriteRouteForUser(Long rideId, Long userId) {

        Ride ride = rideRepository.findRideDetailsForUser(rideId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));

        Route route = ride.getRoute();
        if (route == null || route.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route not found");
        }

        RegisteredUser ru = registeredUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Long routeId = route.getId();

        boolean alreadyFav = ru.getFavoriteRoutes().stream()
                .anyMatch(r -> r.getId() != null && r.getId().equals(routeId));

        if (alreadyFav) {
            ru.getFavoriteRoutes().removeIf(r -> r.getId() != null && r.getId().equals(routeId));
            registeredUserRepository.save(ru);
            return false;
        } else {
            Route managedRoute = routeRepository.findById(routeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found"));
            ru.getFavoriteRoutes().add(managedRoute);
            registeredUserRepository.save(ru);
            return true;
        }
    }

    @Override
    public Ride createRideFromHistory(User loggedUser, CreateRideFromHistoryDTO dto) {
        if (loggedUser instanceof RegisteredUser registeredUser) {
            if (registeredUser.isBlocked()) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        registeredUser.getBlockingReason() != null
                                ? "ACCOUNT_BLOCKED: " + registeredUser.getBlockingReason()
                                : "ACCOUNT_BLOCKED"
                );
            }
        }


        Route route = routeRepository.findById(dto.getRouteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found"
                ));


        Driver driver = findDriver(route.getPickup(), route.getDropoff());
        if (driver == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "NO_AVAILABLE_DRIVER"
            );
        }


        Ride ride = new Ride();
        ride.setStatus(RideStatus.SCHEDULED);
        ride.setCreationTime(LocalDateTime.now());

        // Ako je scheduledTime null, postavi ga na sada
        if (dto.getScheduledTime() != null) {
            ride.setStartTime(dto.getScheduledTime());
        } else {
            ride.setStartTime(LocalDateTime.now());
        }

        ride.setRoute(route);
        ride.setCreator((RegisteredUser) loggedUser);
        ride.setDriver(driver);

        double price = calculatePrice(10.0, VehicleType.STANDARD);
        ride.setPrice(price);


        ride.setOtherPassengers(List.of());

        return rideRepository.save(ride);
    }


    private Driver findDriver(Location pickup, Location dropoff) {
        List<Driver> drivers = driverRepository.findActiveDriversWithVehicle();

        return drivers.stream()
                .filter(d -> !d.isBlocked())
                .filter(d -> d.getStatus() == DriverStatus.ACTIVE)
                .findFirst()
                .orElse(null);
    }

    @Override
    public TrackingRideDTO getTrackingRideDTO(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException(username);
        }
        List<Ride> rides = rideRepository.findActiveRidesByUser(user.getId());
        if (rides.isEmpty()) {
            throw new RideNotFoundException("No active ride found for user " + username);
        }
        if (rides.size() > 1) {
            throw new InvalidRideStateException("Multiple active rides found for user " + username);
        }
        Ride ride = rides.get(0);
        return new TrackingRideDTO(ride);

    }

    @Override
    public TrackingRideDTO getTrackingRideDTOForAdmin(Long driverId) {
        Optional<Driver> driver = driverRepository.findById(driverId);
        if (driver.isEmpty()) {
            throw new UserNotFoundException(driverId.toString());
        }
        List<Ride> rides = rideRepository.findByStatusAndDriver_Id(RideStatus.IN_PROGRESS, driverId);
        if (rides.isEmpty()) {
            throw new RideNotFoundException("No active ride found for user " + driverId);
        }
        if (rides.size() > 1) {
            throw new InvalidRideStateException("Multiple active rides found for user " + driverId);
        }
        Ride ride = rides.get(0);
        return new TrackingRideDTO(ride);

    }
    @Override
    @Transactional
    public Ride stopRide(Long rideId, User currentUser, StopRideRequestDTO req) {

        if (rideId == null || rideId <= 0) {
            throw new IllegalArgumentException("ID vožnje mora biti pozitivan broj");
        }

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Vožnja sa ID " + rideId + " nije pronađena"));

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new InvalidRideStateException("Vožnja nije u toku. Trenutni status: " + ride.getStatus());
        }

        // autorizacija (možeš ostaviti samo driver ako želite striktno)
        boolean isDriver = ride.getDriver() != null && ride.getDriver().getId().equals(currentUser.getId());
        boolean isCreator = ride.getCreator() != null && ride.getCreator().getId().equals(currentUser.getId());
        boolean isPassenger = ride.getOtherPassengers() != null &&
                ride.getOtherPassengers().stream().anyMatch(p -> p.getId().equals(currentUser.getId()));

        if (!isDriver && !isCreator && !isPassenger) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nemate dozvolu da zaustavite ovu vožnju");
        }

        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing stop payload");
        }

        LocalDateTime stopTime = (req.getStoppedAt() != null) ? req.getStoppedAt() : LocalDateTime.now();
        double stopLat = req.getLat();
        double stopLng = req.getLng();

        // 1) end time
        ride.setEndTime(stopTime);

        // 2) route + dropoff update
        Route route = ride.getRoute();
        if (route == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ride has no route");
        }

        if (route.getDropoff() == null) {
            route.setDropoff(new Location());
        }

        route.getDropoff().setLatitude(stopLat);
        route.getDropoff().setLongitude(stopLng);
        //route.getDropoff().setAddress(stopLat + ", " + stopLng);
        String addr = (req != null && req.getAddress() != null && !req.getAddress().isBlank())
                ? req.getAddress()
                : (stopLat + ", " + stopLng);

        route.getDropoff().setAddress(addr);


        // 3) distance
        double distanceKm = 0.0;

        boolean hasPickup = route.getPickup() != null
                && route.getPickup().getLatitude() != 0
                && route.getPickup().getLongitude() != 0;

        if (hasPickup) {
            distanceKm = haversineKm(
                    route.getPickup().getLatitude(), route.getPickup().getLongitude(),
                    stopLat, stopLng
            );
        }

        // ako je distance ispala 0 (npr. stop lokacija = pickup), koristi postojeću distance iz rute ako postoji
        if (distanceKm <= 0.01 && route.getDistance() > 0) {
            distanceKm = route.getDistance();
        } else {
            // ažuriraj distance da prati novo odredište
            route.setDistance(distanceKm);
        }

        routeRepository.save(route);

        // 4) price
        VehicleType type = (ride.getDriver() != null && ride.getDriver().getVehicle() != null && ride.getDriver().getVehicle().getType() != null)
                ? ride.getDriver().getVehicle().getType()
                : VehicleType.STANDARD;

        double newPrice = calculatePrice(distanceKm, type);

        // (opciono) fallback ako pricing nije podešen pa ispadne 0
        if (newPrice <= 0 && distanceKm > 0) {
            newPrice = distanceKm * 120; // makar distance komponenta
        }

        ride.setPrice(newPrice);

        // 5) finish ride + driver ACTIVE
        ride.setStatus(RideStatus.FINISHED);

        Driver driver = ride.getDriver();
        if (driver != null) {
            driver.setStatus(DriverStatus.ACTIVE);
            driverRepository.save(driver);
        }

        return rideRepository.save(ride);
    }

    // helper
    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

}



