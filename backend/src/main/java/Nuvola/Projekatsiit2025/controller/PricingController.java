package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.pricing.UpdateVehicleTypePriceDTO;
import Nuvola.Projekatsiit2025.dto.pricing.VehicleTypePricingDTO;
import Nuvola.Projekatsiit2025.model.enums.VehicleType;
import Nuvola.Projekatsiit2025.services.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    @Autowired
    private PricingService pricingService;

    @GetMapping("/vehicle-types")
    public ResponseEntity<List<VehicleTypePricingDTO>> getAllVehicleTypePrices() {
        return ResponseEntity.ok(pricingService.getAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/vehicle-types/{type}")
    public ResponseEntity<VehicleTypePricingDTO> upsertVehicleTypePrice(
            @PathVariable VehicleType type,
            @RequestBody UpdateVehicleTypePriceDTO dto
    ) {
        return ResponseEntity.ok(pricingService.upsert(type, dto.getBasePrice()));
    }
}
