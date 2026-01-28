package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.RegisterRequestDTO;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import Nuvola.Projekatsiit2025.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Override
    public User findById(Long id)  {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
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
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + username));
    }

}
