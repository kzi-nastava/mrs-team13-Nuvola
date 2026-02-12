package Nuvola.Projekatsiit2025.controller;


import Nuvola.Projekatsiit2025.dto.*;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.model.enums.NotificationType;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import Nuvola.Projekatsiit2025.services.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationService notificationService;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 2.3
    @GetMapping
    public ResponseEntity<ProfileResponseDTO> getProfile() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ProfileResponseDTO dto = new ProfileResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setPicture(user.getPicture());

        return ResponseEntity.ok(dto);
    }
    // 2.3
    @PutMapping
    public ResponseEntity<Void> updateProfile(
            @Valid @RequestBody UpdateProfileDTO dto
    ) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String existingPicture = user.getPicture();

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());

        user.setPicture(existingPicture);

        userRepository.save(user);

        return ResponseEntity.ok().build();
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

    @PostMapping("/picture")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            Authentication auth
    ) throws IOException {

        String email = auth.getName(); // email == username
        User user = userRepository.findByUsername(email);

        String filename = "profile_" + user.getId() + ".png";

        Path uploadPath = Paths.get("C:/uploads/profile-pictures/");
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, file.getBytes());

        user.setPicture(filename);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "picture", filename
        ));
    }

    @GetMapping("/picture/{filename}")
    public ResponseEntity<Resource> getProfilePicture(@PathVariable String filename)
            throws IOException {

        Path filePath = Paths.get("C:/uploads/profile-pictures/").resolve(filename);

        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }

    @PutMapping("/notification-test")
    public ResponseEntity<Void> testSendingNotification() {
        notificationService.sendNotification(1L, "Test notification", "This is a test notification", NotificationType.RideReminder);
        return ResponseEntity.ok().build();
    }

}
