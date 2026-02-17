package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long>, JpaSpecificationExecutor<Ride> {
    List<Ride> findByDriverId(Long driverId, Sort sort);
    Page<Ride> findByDriverId(Long driverId, Pageable pageable);
    List<Ride> findByDriver_UsernameAndStatus(String username, RideStatus status);
    Optional<Ride> findFirstByDriverIdAndStatusAndStartTimeIsNotNullAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
            Long driverId, RideStatus status, LocalDateTime time
    );

    List<Ride> findByDriver_UsernameAndStatusIn(String username, List<RideStatus> statuses);
    @Query("SELECT r FROM Ride r " +
            "WHERE r.status = 'IN_PROGRESS' " +
            "AND (r.creator.id = :userId " +
            "     OR EXISTS (SELECT p FROM r.otherPassengers p WHERE p.id = :userId))")
    List<Ride> findActiveRidesByUser(@Param("userId") Long userId);

    List<Ride> findByIsPanicTrue();

    @Query("SELECT DISTINCT r FROM Ride r " +
            "LEFT JOIN FETCH r.route " +
            "LEFT JOIN FETCH r.driver d " +
            "LEFT JOIN FETCH r.reports " +
            "LEFT JOIN FETCH r.ratings " +
            "LEFT JOIN r.otherPassengers p " +
            "WHERE r.id = :rideId AND (r.creator.id = :userId OR p.id = :userId)")
    Optional<Ride> findRideDetailsForUser(
            @Param("rideId") Long rideId,
            @Param("userId") Long userId
    );
    Page<Ride> findByCreatorIdAndStatus(
            Long creatorId,
            RideStatus status,
            Pageable pageable
    );

}
