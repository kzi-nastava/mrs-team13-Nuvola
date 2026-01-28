package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.FavoriteRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRouteRepository extends JpaRepository<FavoriteRoute, Long> {
    List<FavoriteRoute> findByUserId(Long userId);
}
