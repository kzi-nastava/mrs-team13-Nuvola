package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    @Query("SELECT d FROM Driver d WHERE d.status = 'ACTIVE' AND d.vehicle IS NOT NULL")
    List<Driver> findActiveDriversWithVehicle();

    Optional<Driver> findByVehicleId(Long vehicleId);
    boolean existsByEmail(String email);
}
