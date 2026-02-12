package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.CreateReportDTO;
import Nuvola.Projekatsiit2025.dto.CreateRideDTO;
import Nuvola.Projekatsiit2025.dto.DriverRideHistoryItemDTO;
import Nuvola.Projekatsiit2025.dto.ScheduledRideDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RideServiceImpl implements RideService {
    @Autowired
    private RideRepository rideRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    @Autowired
    private ReportRepository reportRepository;


    @Override
    public Page<DriverRideHistoryItemDTO> getDriverRideHistory(Long driverId, String sortBy, String sortOrder, Integer page, Integer size) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // if pagination
        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Ride> ridesPage = rideRepository.findByDriverId(driverId, pageable);

            return ridesPage.map(DriverRideHistoryItemDTO::new);
        }

        List<Ride> rides = rideRepository.findByDriverId(driverId, sort);
        List<DriverRideHistoryItemDTO> dtos = rides.stream()
                .map(DriverRideHistoryItemDTO::new)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, PageRequest.of(0, dtos.size(), sort), dtos.size());
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
        rideRepository.save(ride);

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

        Location dropoff = new Location();
        dropoff.setLatitude(dto.getTo().getLatitude());
        dropoff.setLongitude(dto.getTo().getLongitude());

        route.setPickup(pickup);
        route.setDropoff(dropoff);

        List<Location> stopLocations = dto.getStops().stream().map(s -> {
            Location l = new Location();
            l.setLatitude(s.getLatitude());
            l.setLongitude(s.getLongitude());
            return l;
        }).toList();

        route.setStops(stopLocations);
        route.setFavourite(false);

        return route;
    }

    private Driver findDriver(CreateRideDTO dto) {
        List<Driver> drivers = driverRepository.findActiveDriversWithVehicle();

        return drivers.stream()
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
        Route route = createRoute(dto);

        Driver driver = findDriver(dto);
        if (driver == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "NO_AVAILABLE_DRIVER"
            );
        }

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

        return rideRepository.save(ride);
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

        // send email to passengers
        String messageBody = "Ride ID: " + ride.getId() + "\n" + "Price: " + ride.getPrice() + " RSD\n";
        EmailDetails emailDetails = new EmailDetails("", messageBody, "Ride Ended");
        for (RegisteredUser u : ride.getOtherPassengers()) {
            emailDetails.setRecipient(u.getEmail());
            emailService.sendRideFinished(emailDetails);
        }
        emailDetails.setRecipient(ride.getCreator().getEmail());
        emailService.sendRideFinished(emailDetails);

        //TODO: send notification to passengers

        Ride scheduledRide = getNearestScheduledRideForDriver(driver.getId());
        if  (scheduledRide == null) {
            return null;
        }

        return scheduledRide.getId();
    }

    @Override
    public void createReport(CreateReportDTO createReportDTO) {
        Report report = new Report();
        RegisteredUser author = registeredUserRepository.findByUsername(createReportDTO.getAuthorUsername());
        if (author == null) throw new UserNotFoundException("Registered user " + createReportDTO.getAuthorUsername() + " not found");
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
        return new  ScheduledRideDTO(ride);
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
        double basePrice;

        switch (type) {
            case STANDARD -> basePrice = 250;
            case LUXURY -> basePrice = 450;
            case VAN -> basePrice = 350;
            default -> basePrice = 250;
        }

        return basePrice + distanceKm * 120;
    }

}
