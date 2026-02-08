package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.RegisteredUser;
import Nuvola.Projekatsiit2025.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    List<RegisteredUser> findByEmailIn(List<String> emails);

    Optional<User> findByEmailIgnoreCase(String email);
}
