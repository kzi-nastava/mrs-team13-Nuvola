package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.*;
import Nuvola.Projekatsiit2025.model.ActivationToken;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.model.enums.DriverStatus;
import Nuvola.Projekatsiit2025.repositories.ActivationTokenRepository;
import Nuvola.Projekatsiit2025.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    // 2.2.1 Login (email + password)
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {

        // if driver has "driver" in email, we know that user is driver
        boolean isDriver = dto.getEmail() != null && dto.getEmail().toLowerCase().contains("driver");

        LoginResponseDTO response = new LoginResponseDTO();
        response.setId(1L);
        response.setEmail(dto.getEmail());
        response.setFirstName("Test");
        response.setLastName(isDriver ? "Driver" : "User");
        response.setToken("fake-token-123");
        response.setUserType(isDriver ? "DRIVER" : "USER");

        if (isDriver) {
            // when driver do login he is gonna be active in system
            response.setDriverStatus(DriverStatus.ACTIVE);
            response.setMessage("Driver logged in and is now ACTIVE.");
        } else {
            response.setDriverStatus(null);
            response.setMessage("User logged in.");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 2.2.1 Logout
    // driver can't logout if he has active ride
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestParam(defaultValue = "false") boolean hasActiveRide
    ) {
        if (hasActiveRide) {
            return new ResponseEntity<>("Driver cannot logout while having an active ride.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Logout successful.", HttpStatus.OK);
    }

    // 2.2.1 Forgot password (email sent)
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequestDTO dto) {
        return new ResponseEntity<>("Password reset email sent (stub).", HttpStatus.ACCEPTED);
    }

    // 2.2.1 Reset password (tocken from mail)
    @PostMapping("/reset-password/{token}")
    public ResponseEntity<String> resetPassword(@PathVariable String token,
                                                @RequestBody ResetPasswordRequestDTO dto) {
        return new ResponseEntity<>("Password has been reset (stub).", HttpStatus.OK);
    }

    
    // 2.2.1 Driver change status active/inactive while user is on his profile
    // if he change into INACTIVE while he has a ride, he is gonna be INACTIVE after that ride
    @PutMapping("/driver/status")
    public ResponseEntity<String> changeDriverStatus(
            @RequestParam(defaultValue = "false") boolean hasActiveRide,
            @RequestBody ChangeDriverStatusDTO dto
    ) {
        if (hasActiveRide && dto.getStatus() == DriverStatus.INACTIVE) {
            return new ResponseEntity<>("Driver will become INACTIVE after current ride ends (stub).", HttpStatus.OK);
        }
        return new ResponseEntity<>("Driver status changed to " + dto.getStatus() + " (stub).", HttpStatus.OK);
    }
      
    // 2.2.2 Registration user
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody RegisterRequestDTO dto) {

        RegisterResponseDTO response = new RegisterResponseDTO();
        response.setId(100L);
        response.setEmail(dto.getEmail());
        response.setFirstName(dto.getFirstName());
        response.setLastName(dto.getLastName());
        response.setAddress(dto.getAddress());
        response.setPhone(dto.getPhone());
        response.setPicture(dto.getPicture());
        response.setMessage("User successfully registered.");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    // 2.2.3 Driver registration

    @PostMapping("/activate")
    public ResponseEntity<Void> activateAccount(
            @RequestParam String token,
            @RequestParam String password
    ) {
        ActivationToken activationToken =
                activationTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "INVALID_TOKEN"
                                ));

        if (activationToken.isUsed()
                || activationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "TOKEN_EXPIRED"
            );
        }

        User user = activationToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        user.setBlocked(false);

        userRepository.save(user);

        activationToken.setUsed(true);
        activationTokenRepository.save(activationToken);

        return ResponseEntity.ok().build();
    }
}






