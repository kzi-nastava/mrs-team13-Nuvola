package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.RegisterRequestDTO;
import Nuvola.Projekatsiit2025.model.User;
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
        return null;
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
