package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.VehicleTypePricing;
import Nuvola.Projekatsiit2025.model.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleTypePricingRepository extends JpaRepository<VehicleTypePricing, VehicleType> {
}
