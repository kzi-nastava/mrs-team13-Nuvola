package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.model.PasswordResetToken;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.repositories.PasswordResetTokenRepository;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import Nuvola.Projekatsiit2025.services.EmailService;
import Nuvola.Projekatsiit2025.services.PasswordResetService;
import Nuvola.Projekatsiit2025.util.EmailDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.reset.base-url:http://localhost:8080/api/auth/reset-password/open?token=}")
    private String resetBaseUrl;

    @Value("${app.reset.token-minutes:30}")
    private long tokenMinutes;

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public void requestReset(String email) {
        if (email == null || email.isBlank()) return;

        var maybeUser = userRepository.findByEmailIgnoreCase(email.trim());

        if (maybeUser.isEmpty()) {
            // security: ne otkrivamo da ne postoji
            return;
        }

        User user = maybeUser.get();

        // samo 1 aktivan token
        tokenRepository.deleteAllByUser(user);

        String token = UUID.randomUUID().toString();

        PasswordResetToken prt = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(tokenMinutes))
                .used(false)
                .build();

        tokenRepository.save(prt);

        String link = resetBaseUrl + token; // ide na /reset-password/open?token=...
        EmailDetails details = new EmailDetails();
        details.setRecipient(user.getEmail());
        details.setSubject("Reset lozinke - Nuvola");

        details.setMsgBody("http://localhost:4200/reset-password?token=" + token);

        emailService.sendPasswordReset(details);

    }

    @Override
    public void resetPassword(String token, String newPassword, String confirmNewPassword) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_REQUIRED");
        }
        if (newPassword == null || confirmNewPassword == null ||
                newPassword.isBlank() || confirmNewPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PASSWORD_REQUIRED");
        }
        if (!newPassword.equals(confirmNewPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH");
        }

        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_TOKEN"));

        if (prt.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_USED");
        }
        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_EXPIRED");
        }

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        prt.setUsed(true);
        tokenRepository.save(prt);
    }
}
