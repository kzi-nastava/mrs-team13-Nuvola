package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.RegisterRequestDTO;
import Nuvola.Projekatsiit2025.model.ActivationToken;
import Nuvola.Projekatsiit2025.model.Chat;
import Nuvola.Projekatsiit2025.model.RegisteredUser;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.repositories.ActivationTokenRepository;
import Nuvola.Projekatsiit2025.repositories.RegisteredUserRepository;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import Nuvola.Projekatsiit2025.services.EmailService;
import Nuvola.Projekatsiit2025.services.UserService;
import Nuvola.Projekatsiit2025.util.EmailDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RegisteredUserRepository registeredUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private EmailService emailService;


    @Override
    public User findById(Long id)  {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> findAll()  {
        return userRepository.findAll();
    }

    @Override
    public User save(RegisterRequestDTO registerRequest) {

//        RegisteredUser newUser = new RegisteredUser();
//        newUser.setEmail(registerRequest.getEmail());
//        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
//        newUser.setFirstName(registerRequest.getFirstName());
//        newUser.setLastName(registerRequest.getLastName());
//        newUser.setAddress(registerRequest.getAddress());
//        newUser.setPhone(registerRequest.getPhone());
//        newUser.setPicture(registerRequest.getPicture());
//        userRepository.save(newUser);
        return null;
    }

    @Override
    public RegisteredUser saveRegisteredUser(RegisterRequestDTO registerRequest) {

        if (registerRequest.getPassword() == null || registerRequest.getConfirmPassword() == null
                || !registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("PASSWORDS_DO_NOT_MATCH");
        }

        RegisteredUser newUser = new RegisteredUser();
        newUser.setEmail(registerRequest.getEmail());
        newUser.setUsername(registerRequest.getUsername()); // email
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setAddress(registerRequest.getAddress());
        newUser.setPhone(registerRequest.getPhone());
        newUser.setBlocked(false);
        newUser.setBlockingReason("");

        // default slika ako nije uneta
        String pic = registerRequest.getPicture();
        if (pic == null || pic.isBlank()) {
            pic = "/images/default-user.png";
        }
        newUser.setPicture(pic);

        Chat chat = new Chat();
        chat.setOwner(newUser);
        newUser.setChat(chat);

        // mora biti false dok se ne aktivira preko mejla
        newUser.setActivated(false);

        RegisteredUser saved = registeredUserRepository.save(newUser);

        //  kreiraj token koji traje 24h
        String tokenValue = UUID.randomUUID().toString();
        ActivationToken token = new ActivationToken();
        token.setToken(tokenValue);
        token.setUser(saved);
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        token.setUsed(false);
        activationTokenRepository.save(token);

        //  pošalji mejl sa linkom
        String link = "http://localhost:8080/api/auth/activate-email?token=" + tokenValue;


        EmailDetails details = new EmailDetails(
                saved.getEmail(),
                "Hello " + saved.getFirstName() + ",\n\n" +
                        "Please activate your account by clicking the link below (valid for 24h):\n" +
                        link + "\n\n" +
                        "Nuvola Team",
                "Activate your Nuvola account"
        );

        emailService.sendSimpleMail(details);

        return saved;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        }


        if (user instanceof RegisteredUser ru && !ru.isActivated()) {
            throw new UsernameNotFoundException("ACCOUNT_NOT_ACTIVATED");
            // alternativno: throw new DisabledException("ACCOUNT_NOT_ACTIVATED");
        }

        return user;
    }

}
