package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByDriverId(Long driverId, Sort sort);
    Page<Ride> findByDriverId(Long driverId, Pageable pageable);
    List<Ride> findByDriver_UsernameAndStatus(String username, RideStatus status);
    Optional<Ride> findFirstByDriverIdAndStatusAndStartTimeIsNotNullAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
            Long driverId, RideStatus status, LocalDateTime time
    );


}
