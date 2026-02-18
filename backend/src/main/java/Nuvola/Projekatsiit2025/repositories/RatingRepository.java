package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Integer> {
    List<Rating> findByRideId(Long rideId);
    boolean existsByRideIdAndUserId(Long rideId, Long userId);
}
