package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.*;
import Nuvola.Projekatsiit2025.exceptions.ResourceConflictException;
import Nuvola.Projekatsiit2025.model.ActivationToken;
import Nuvola.Projekatsiit2025.model.RegisteredUser;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.model.enums.DriverStatus;
import Nuvola.Projekatsiit2025.repositories.ActivationTokenRepository;
import Nuvola.Projekatsiit2025.repositories.UserRepository;

import Nuvola.Projekatsiit2025.services.DriverService;
//import Nuvola.Projekatsiit2025.services.PasswordResetService;
import Nuvola.Projekatsiit2025.services.PasswordResetService;
import Nuvola.Projekatsiit2025.services.UserService;
import Nuvola.Projekatsiit2025.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    //@Autowired
    //private UserService userService;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private DriverService driverService;

    @Autowired
    private PasswordResetService passwordResetService;


    // 2.2.1 Login (email + password)
    @PostMapping("/login")
    public ResponseEntity<UserTokenState> login(@RequestBody LoginRequestDTO dto) {
        // if credentials are not correct then AuthenticationException
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                dto.getUsername(), dto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate token for user
        User user = (User) authentication.getPrincipal();
        String jwt = tokenUtils.generateToken(user);
        int expiresIn = tokenUtils.getExpiredIn();

        if (user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DRIVER"))) {
            driverService.loginDriver(user.getId());
        }

        return ResponseEntity.ok(new UserTokenState(jwt, expiresIn));
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
    @PostMapping(value = "/forgot-password", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequestDTO dto) {

        if (dto == null || dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "EMAIL_REQUIRED");
        }

        passwordResetService.requestReset(dto.getEmail().trim());

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body("Ako email postoji u sistemu, poslat je link za reset lozinke.");
    }



    // 2.2.1 Reset password (tocken from mail)
//    @PostMapping(value = "/reset-password", produces = MediaType.TEXT_PLAIN_VALUE)
//    public ResponseEntity<String> resetPassword(@RequestParam String token,
//                                                @RequestBody ResetPasswordRequestDTO dto) {
//
//        if (dto == null) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "BODY_REQUIRED");
//        }
//
//        passwordResetService.resetPassword(
//                token,
//                dto.getNewPassword(),
//                dto.getConfirmNewPassword()
//
//        );
//
//        return ResponseEntity.ok("Password has been reset successfully.");
//    }


    // 2.2.1 Reset password (tocken from mail)
    //@PostMapping("/reset-password/{token}")
    //public ResponseEntity<String> resetPassword(@PathVariable String token,
      //                                          @RequestBody ResetPasswordRequestDTO dto) {
        //return new ResponseEntity<>("Password has been reset (stub).", HttpStatus.OK);
    //}


    @PostMapping(value = "/reset-password", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> resetPassword(@RequestParam String token,
                                                @RequestBody ResetPasswordRequestDTO dto) {

        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "BODY_REQUIRED");
        }

        passwordResetService.resetPassword(
                token,
                dto.getNewPassword(),
                dto.getConfirmNewPassword()
        );

        return ResponseEntity.ok("Password has been reset successfully.");
    }

    @PostMapping("/reset-password/{token}")
    public ResponseEntity<String> driverSetPassword(@PathVariable String token,
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

        User existUser = this.userService.findByUsername(dto.getUsername());

        if (existUser != null) {
            throw new ResourceConflictException(dto.getUsername(), "Username already exists");
        }

        RegisteredUser user = userService.saveRegisteredUser(dto);

        RegisterResponseDTO response = new RegisterResponseDTO();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setAddress(user.getAddress());
        response.setPhone(user.getPhone());
        response.setMessage("User successfully registered.");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/activate-email")
    public ResponseEntity<String> activateEmail(@RequestParam String token) {

        ActivationToken activationToken =
                activationTokenRepository.findByToken(token)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "INVALID_TOKEN"
                        ));

        if (activationToken.isUsed() ||
                activationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_EXPIRED");
        }

        User user = activationToken.getUser();


        if (user instanceof RegisteredUser ru) {
            ru.setActivated(true);
            userRepository.save(ru);
        } else {

            userRepository.save(user);
        }

        activationToken.setUsed(true);
        activationTokenRepository.save(activationToken);

        return ResponseEntity.ok("Account activated successfully.");
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


    @GetMapping(value = "/reset-password/open", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> openResetPassword(@RequestParam String token) {

        // deep link (app)
        String appLink = "nuvola://reset-password?token=" + token;

        // web fallback (frontend)
        String webLink = "http://localhost:4200/reset-password?token=" + token;

        String html = "<!doctype html><html><head>" +
                "<meta charset='utf-8'/>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1'/>" +
                "<title>Reset</title>" +
                "</head><body style='font-family:Arial,sans-serif;padding:24px;'>" +
                "<h2>Otvaram Nuvola aplikaciju...</h2>" +
                "<p>Ako se aplikacija ne otvori automatski, bićeš preusmeren na web.</p>" +

                "<script>" +
                "var app = " + jsString(appLink) + ";" +
                "var web = " + jsString(webLink) + ";" +
                "var t = setTimeout(function(){ window.location.href = web; }, 900);" +
                "window.location.href = app;" +
                "setTimeout(function(){ clearTimeout(t); }, 1200);" +
                "</script>" +

                "<p><a href='" + appLink + "'>Otvori u aplikaciji</a></p>" +
                "<p><a href='" + webLink + "'>Otvori web link</a></p>" +
                "</body></html>";

        return ResponseEntity.ok(html);
    }

    private String jsString(String s) {
        return "'" + s.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }


}






