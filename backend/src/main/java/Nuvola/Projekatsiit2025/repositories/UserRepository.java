package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
