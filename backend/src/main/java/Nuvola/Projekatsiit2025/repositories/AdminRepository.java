package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
}
