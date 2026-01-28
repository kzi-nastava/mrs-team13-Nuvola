package Nuvola.Projekatsiit2025.controller;


import Nuvola.Projekatsiit2025.dto.*;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setPicture(dto.getPicture());

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
}
