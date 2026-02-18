package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.RegisteredUser;
import Nuvola.Projekatsiit2025.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegisteredUserRepository extends JpaRepository<RegisteredUser, Long> {
    RegisteredUser findByUsername(String username);
    List<RegisteredUser> findByEmailIn(List<String> emails);
}
