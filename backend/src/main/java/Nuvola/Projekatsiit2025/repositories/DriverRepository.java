package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    @Query("SELECT d FROM Driver d WHERE d.status = 'ACTIVE' AND d.vehicle IS NOT NULL")
    List<Driver> findActiveDriversWithVehicle();

    @Query("SELECT d FROM Driver d WHERE d.vehicle IS NOT NULL AND d.status IN ('ACTIVE', 'BUSY')")
    List<Driver> findActiveAndBusyDriversWithVehicle();

    Optional<Driver> findByVehicleId(Long vehicleId);
    boolean existsByEmail(String email);

    @Query("""
        SELECT d
        FROM Driver d
        WHERE (:search IS NULL OR :search = ''
               OR (
                    LOWER(CONCAT(d.firstName, ' ', d.lastName)) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(CONCAT(d.lastName, ' ', d.firstName)) LIKE LOWER(CONCAT('%', :search, '%'))
               )
        )
        """)
    List<Driver> searchDriversByFullName(@Param("search") String search);

    Optional<Driver> findByEmail(String email);

}
