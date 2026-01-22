package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.RegisterRequestDTO;
import Nuvola.Projekatsiit2025.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface UserService extends UserDetailsService {
    User findById(Long id);
    User findByUsername(String username);
    List<User> findAll ();
    User save(RegisterRequestDTO registerRequest);
}
