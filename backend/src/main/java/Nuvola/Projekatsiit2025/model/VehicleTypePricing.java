package Nuvola.Projekatsiit2025.model;

import Nuvola.Projekatsiit2025.model.enums.VehicleType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@Table(name = "vehicle_type_pricing")
public class VehicleTypePricing {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType;

    @Column(name = "base_price", nullable = false)
    private Double basePrice;

    protected VehicleTypePricing() {}

    public VehicleTypePricing(VehicleType vehicleType, Double basePrice) {
        this.vehicleType = vehicleType;
        this.basePrice = basePrice;
    }


}
