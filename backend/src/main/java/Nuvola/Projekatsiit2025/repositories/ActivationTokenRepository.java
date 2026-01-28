package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.ActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivationTokenRepository
        extends JpaRepository<ActivationToken, Long> {

    Optional<ActivationToken> findByToken(String token);
}
