package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.Ride;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByDriverId(Long driverId, Sort sort);
}
