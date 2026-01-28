package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.*;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.model.enums.DriverStatus;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import Nuvola.Projekatsiit2025.util.TokenUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final TokenUtils tokenUtils;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          TokenUtils tokenUtils,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.tokenUtils = tokenUtils;
        this.userRepository = userRepository;
    }

    // 2.2.1 Login (email + password) - REAL
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"message\":\"Invalid email or password\"}");
        }

        // 1️⃣ Učitaj User iz baze
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2️⃣ Generiši JWT (OVO JE KLJUČNO)
        String token = tokenUtils.generateToken(user);

        // 3️⃣ Izvuci rolu iz authorities
        String role = user.getAuthorities()
                .stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("ROLE_USER")
                .replace("ROLE_", "");

        // 4️⃣ Response
        LoginResponseDTO response = new LoginResponseDTO();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setToken(token);
        response.setUserType(role);

        if ("DRIVER".equals(role)) {
            response.setDriverStatus(DriverStatus.ACTIVE);
            response.setMessage("Driver logged in and is now ACTIVE.");
        } else {
            response.setDriverStatus(null);
            response.setMessage("User logged in.");
        }

        return ResponseEntity.ok(response);
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






