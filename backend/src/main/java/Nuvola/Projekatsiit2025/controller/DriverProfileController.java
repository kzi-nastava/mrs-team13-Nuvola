package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.ChangePasswordDTO;
import Nuvola.Projekatsiit2025.dto.DriverProfileResponseDTO;
import Nuvola.Projekatsiit2025.dto.DriverProfileUpdateDTO;
import Nuvola.Projekatsiit2025.dto.ProfileResponseDTO;
import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.ProfileChangeRequest;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.model.enums.RequestStatus;
import Nuvola.Projekatsiit2025.repositories.ProfileChangeRequestRepository;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;



@RestController
@RequestMapping("/api/driver/profile")
@CrossOrigin(origins = "*")
public class DriverProfileController {

    private final UserRepository userRepository;
    private final ProfileChangeRequestRepository requestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public DriverProfileController(
            UserRepository userRepository,
            ProfileChangeRequestRepository requestRepository
    ) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
    }

    @GetMapping
    public ResponseEntity<DriverProfileResponseDTO> getDriverProfile() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(email);

        if (!(user instanceof Driver driver)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        DriverProfileResponseDTO dto = new DriverProfileResponseDTO();

        // user
        dto.setId(driver.getId());
        dto.setEmail(driver.getEmail());
        dto.setFirstName(driver.getFirstName());
        dto.setLastName(driver.getLastName());
        dto.setPhone(driver.getPhone());
        dto.setAddress(driver.getAddress());
        dto.setPicture(driver.getPicture());
        dto.setBlocked(driver.isBlocked());
        dto.setBlockingReason(driver.getBlockingReason());

        // vehicle
        dto.setModel(driver.getVehicle().getModel());
        dto.setType(driver.getVehicle().getType());
        dto.setRegNumber(driver.getVehicle().getRegNumber());
        dto.setNumOfSeats(driver.getVehicle().getNumOfSeats());
        dto.setBabyFriendly(driver.getVehicle().isBabyFriendly());
        dto.setPetFriendly(driver.getVehicle().isPetFriendly());

        return ResponseEntity.ok(dto);
    }

    @PutMapping
    public ResponseEntity<Void> requestProfileChange(
            @Valid @RequestBody DriverProfileUpdateDTO dto
    ) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(email);

        if (!(user instanceof Driver driver)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ProfileChangeRequest req = new ProfileChangeRequest();
        req.setDriver(driver);

        req.setFirstName(dto.getFirstName());
        req.setLastName(dto.getLastName());
        req.setPhone(dto.getPhone());
        req.setAddress(dto.getAddress());

        req.setModel(dto.getModel());
        req.setType(dto.getType());
        req.setNumOfSeats(dto.getNumOfSeats());
        req.setBabyFriendly(dto.isBabyFriendly());
        req.setPetFriendly(dto.isPetFriendly());

        req.setStatus(RequestStatus.PENDING);
        req.setCreatedAt(LocalDateTime.now());

        requestRepository.save(req);

        return ResponseEntity.accepted().build(); // 202
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordDTO dto
    ) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CURRENT_PASSWORD_INVALID"
            );
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setLastPasswordResetDate(new java.sql.Timestamp(System.currentTimeMillis()));

        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
}
