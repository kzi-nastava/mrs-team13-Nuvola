package Nuvola.Projekatsiit2025.repositories;
import Nuvola.Projekatsiit2025.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    boolean existsByRegNumber(String regNumber);
}
