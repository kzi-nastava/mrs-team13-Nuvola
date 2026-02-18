package Nuvola.Projekatsiit2025.dto.pricing;

import Nuvola.Projekatsiit2025.model.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleTypePricingDTO {
    VehicleType vehicleType;
    Double basePrice;
}
