package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.CreateDriverDTO;
import Nuvola.Projekatsiit2025.dto.position.DriverPositionUpdateDTO;
import Nuvola.Projekatsiit2025.exceptions.UserNotFoundException;
import Nuvola.Projekatsiit2025.model.Chat;
import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.Vehicle;
import Nuvola.Projekatsiit2025.model.ActivationToken;
import Nuvola.Projekatsiit2025.model.enums.DriverStatus;
import Nuvola.Projekatsiit2025.repositories.DriverRepository;
import Nuvola.Projekatsiit2025.repositories.ActivationTokenRepository;
import Nuvola.Projekatsiit2025.repositories.VehicleRepository;
import Nuvola.Projekatsiit2025.services.DriverService;
import Nuvola.Projekatsiit2025.services.EmailService;
import Nuvola.Projekatsiit2025.util.VehicleLocationStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.UUID;

import Nuvola.Projekatsiit2025.util.EmailDetails;



@Service
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DriverServiceImpl(
            DriverRepository driverRepository,
            VehicleRepository vehicleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

//    @Autowired
//    private VehicleLocationStore vehicleLocationStore;

    @Override
    public Driver createDriver(CreateDriverDTO dto) {

        if (driverRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS");
        }

        if (vehicleRepository.existsByRegNumber(dto.getRegNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "REG_NUMBER_ALREADY_EXISTS");
        }

        Driver driver = new Driver();
        driver.setEmail(dto.getEmail());
        driver.setUsername(dto.getEmail());
        driver.setFirstName(dto.getFirstName());
        driver.setLastName(dto.getLastName());
        driver.setPhone(dto.getPhone());
        driver.setAddress(dto.getAddress());
        driver.setPicture(dto.getPicture());
        driver.setBlocked(false);
        driver.setStatus(DriverStatus.ACTIVE);
        driver.setPassword("TEMP");

        Vehicle vehicle = new Vehicle();
        vehicle.setModel(dto.getModel());
        vehicle.setType(dto.getType());
        vehicle.setRegNumber(dto.getRegNumber());
        vehicle.setNumOfSeats(dto.getNumOfSeats());
        vehicle.setBabyFriendly(dto.isBabyFriendly());
        vehicle.setPetFriendly(dto.isPetFriendly());

        driver.setVehicle(vehicle);

        Chat chat = new Chat();
        chat.setOwner(driver); // Setting the driver as the owner of the chat
        driver.setChat(chat);  // Assign chat to the driver

        Driver savedDriver = driverRepository.save(driver);

        String token = UUID.randomUUID().toString();

        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken(token);
        activationToken.setUser(savedDriver);
        activationToken.setExpiresAt(LocalDateTime.now().plusHours(24));

        activationTokenRepository.save(activationToken);

        String activationLink =
                "http://localhost:4200/driver-set-password?token=" + token;

        EmailDetails mail = new EmailDetails();
        mail.setRecipient(savedDriver.getEmail());
        mail.setSubject("Activate your Nuvola driver account");
        mail.setMsgBody(
                "Your driver account has been created.\n\n" +
                        "Click the link below to activate your account:\n" +
                        activationLink + "\n\n" +
                        "This link is valid for 24 hours."
        );

        emailService.sendSimpleMail(mail);

        return savedDriver;
    }

    @Override
    public void logoutDriver(Long driverId) {
        // TODO: add more logout logic (e.g. activity session, etc.)
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new UserNotFoundException("Driver " + driverId + " not found"));
        driver.setStatus(DriverStatus.INACTIVE);
        driverRepository.save(driver);

        // Update driver status to INACTIVE for web socket clients
        DriverPositionUpdateDTO update = new DriverPositionUpdateDTO();
        update.setDriverId(driverId);
        update.setToRemove(true);
        simpMessagingTemplate.convertAndSend("/topic/position/" + driverId, update);
        simpMessagingTemplate.convertAndSend("/topic/position/all", update);
        //vehicleLocationStore.remove(driverId);
    }

    @Override
    public void loginDriver(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new UserNotFoundException("Driver " + driverId + " not found"));
        driver.setStatus(DriverStatus.ACTIVE);
        driverRepository.save(driver);

        // TODO: add more login logic (e.g. activity session, etc.)
    }

}
