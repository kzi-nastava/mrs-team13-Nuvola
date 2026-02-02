package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.RegisterRequestDTO;
import Nuvola.Projekatsiit2025.model.Chat;
import Nuvola.Projekatsiit2025.model.RegisteredUser;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.repositories.RegisteredUserRepository;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import Nuvola.Projekatsiit2025.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RegisteredUserRepository registeredUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


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
        RegisteredUser newUser = new RegisteredUser();
        newUser.setEmail(registerRequest.getEmail());
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setAddress(registerRequest.getAddress());
        newUser.setPhone(registerRequest.getPhone());
        newUser.setPicture(registerRequest.getPicture());
        newUser.setBlocked(false);
        newUser.setBlockingReason("");

        Chat chat =  new Chat();
        chat.setOwner(newUser);
        newUser.setChat(chat);

        // TODO: Ovo staviti na false kada se odradi aktivacija!!!!!!!!!!!!!!!!
        newUser.setActivated(true);
        return registeredUserRepository.save(newUser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
        } else {
            return user;
        }
    }
}
