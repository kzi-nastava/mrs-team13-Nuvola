package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.pricing.VehicleTypePricingDTO;
import Nuvola.Projekatsiit2025.model.enums.VehicleType;

import java.util.List;

public interface PricingService {
    VehicleTypePricingDTO getOne(VehicleType type);
    List<VehicleTypePricingDTO> getAll();
    VehicleTypePricingDTO upsert(VehicleType type, Double newPrice);
}
