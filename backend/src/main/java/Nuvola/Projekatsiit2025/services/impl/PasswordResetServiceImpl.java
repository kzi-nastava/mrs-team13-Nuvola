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
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.UUID;

@Transactional
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend.reset-password-url:http://localhost:4200/reset-password}")
    private String resetPasswordUrl;

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
    @Transactional
    @Override
    public void requestReset(String email) {
        // Security: uvek vraćaj "OK" čak i ako email ne postoji (da ne otkrivaš korisnike)
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {

            // obriši stare tokene za tog user-a (da ne ostanu aktivni)
            tokenRepository.deleteByUserId(user.getId());

            String token = UUID.randomUUID().toString();

            PasswordResetToken prt = new PasswordResetToken();
            prt.setToken(token);
            prt.setUser(user);
            prt.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            prt.setUsed(false);

            tokenRepository.save(prt);

            // Link koji ide na FRONT (front čita token i šalje ga backend-u)
            String link = resetPasswordUrl + "?token=" + token;

            String body =
                    "Zahtev za reset lozinke.\n\n" +
                            "Klikni na link da postaviš novu lozinku:\n" + link + "\n\n" +
                            "Link važi 30 minuta. Ako nisi ti tražio reset, ignoriši ovu poruku.";

            emailService.sendSimpleMail(new EmailDetails(user.getEmail(), body, "Reset lozinke"));
        });
    }

    @Override
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NEW_PASSWORD_REQUIRED");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PASSWORDS_DO_NOT_MATCH");
        }

        // (opciono) minimalna pravila
        if (newPassword.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PASSWORD_TOO_SHORT");
        }

        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_TOKEN"));

        if (prt.isUsed() || prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_EXPIRED");
        }

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        prt.setUsed(true);
        tokenRepository.save(prt);
    }
}
