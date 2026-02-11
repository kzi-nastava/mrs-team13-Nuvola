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

    // WEB (Angular)
    @Value("${app.frontend.reset-password-url:http://localhost:4200/reset-password}")
    private String resetPasswordWebUrl;

    // MOBILE (Android deep link)
    @Value("${app.mobile.reset-password-deeplink:nuvola://reset-password}")
    private String resetPasswordDeepLink;
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
    //@Transactional
    //@Override
    //public void requestReset(String email) {
        // Security: uvek vraćaj "OK" čak i ako email ne postoji (da ne otkrivaš korisnike)
      //  userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {

            // obriši stare tokene za tog user-a (da ne ostanu aktivni)
        //    tokenRepository.deleteByUserId(user.getId());

          //  String token = UUID.randomUUID().toString();

            //PasswordResetToken prt = new PasswordResetToken();
    //        prt.setToken(token);
      //      prt.setUser(user);
        //    prt.setExpiresAt(LocalDateTime.now().plusMinutes(30));
          //  prt.setUsed(false);

            //tokenRepository.save(prt);

            // Link koji ide na FRONT (front čita token i šalje ga backend-u)
       //     String webLink = resetPasswordWebUrl + "?token=" + token;
         //   String appLink = resetPasswordDeepLink + "?token=" + token;
            // šaljemo HTML mail (APP + WEB fallback)
           // EmailDetails details = new EmailDetails();
    //        details.setRecipient(user.getEmail());
      //      details.setSubject("Reset lozinke");
        //    details.setLink(appLink);      // PRIMARY → Android app
          //  details.setMsgBody(webLink);   // FALLBACK → Web

     //       emailService.sendPasswordReset(details);


       // });
    //}

    @Transactional
    @Override
    public void requestReset(String email) {

        // Security: uvek isto ponašanje (ne otkrivamo da li user postoji)
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {

            // 1. Obriši stare tokene za tog user-a
            tokenRepository.deleteByUserId(user.getId());

            // 2. Kreiraj novi token
            String token = UUID.randomUUID().toString();

            PasswordResetToken prt = new PasswordResetToken();
            prt.setToken(token);
            prt.setUser(user);
            prt.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            prt.setUsed(false);

            tokenRepository.save(prt);

            // 3. Pripremi linkove
            String webLink = resetPasswordWebUrl + "?token=" + token;
            String appLink = resetPasswordDeepLink + "?token=" + token;

            // 4. Pripremi email
            EmailDetails details = new EmailDetails();
            details.setRecipient(user.getEmail());
            details.setSubject("Reset lozinke");
            details.setLink(appLink);      // PRIMARY → Android deep link
            details.setMsgBody(webLink);   // FALLBACK → Web link

            // 5. Pokušaj slanje maila (ALI NE RUŠI API AKO FAILUJE)
            try {
                emailService.sendPasswordReset(details);
            } catch (Exception e) {
                // LOGUJ, ali NE bacaj exception dalje
                System.err.println("❌ Failed to send password reset email to: " + user.getEmail());
                e.printStackTrace();
            }
        });

        // NEMA return-a, nema exception-a → controller uvek vraća 202
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
