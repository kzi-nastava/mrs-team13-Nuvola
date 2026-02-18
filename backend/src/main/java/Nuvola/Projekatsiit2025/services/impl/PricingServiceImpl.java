package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.pricing.VehicleTypePricingDTO;
import Nuvola.Projekatsiit2025.model.VehicleTypePricing;
import Nuvola.Projekatsiit2025.model.enums.VehicleType;
import Nuvola.Projekatsiit2025.repositories.VehicleTypePricingRepository;
import Nuvola.Projekatsiit2025.services.PricingService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PricingServiceImpl implements PricingService {

    @Autowired
    private  VehicleTypePricingRepository repo;

//    @Override
//    public Double priceFor(VehicleType type) {
//        return repo.findById(type)
//                .orElseThrow(() -> new IllegalStateException("Missing price for " + type))
//                .getBasePrice();
//    }
//
//    @Override
//    @Transactional
//    public void updatePrice(VehicleType type, Double newPrice) {
//        if (newPrice == null || newPrice < 0) {
//            throw new IllegalArgumentException("Price must be >= 0");
//        }
//
//        VehicleTypePricing pricing = repo.findById(type)
//                .orElseGet(() -> new VehicleTypePricing(type, (double) 0));
//
//        pricing.setBasePrice(newPrice);
//
//        repo.saveAndFlush(pricing);
//
//    }

//    @Override
//    @Transactional()
//    public List<VehicleTypePricingDTO> getAll() {
//        return repo.findAll().stream()
//                .map(p -> new VehicleTypePricingDTO(p.getVehicleType(), p.getBasePrice()))
//                .toList();
//    }

    @Override
    @Transactional
    public VehicleTypePricingDTO getOne(VehicleType type) {
        VehicleTypePricing pricing = repo.findById(type)
                .orElseGet(() -> repo.save(new VehicleTypePricing(type, 0.0)));

        return new VehicleTypePricingDTO(pricing.getVehicleType(), pricing.getBasePrice());
    }

    @Override
    @Transactional
    public List<VehicleTypePricingDTO> getAll() {
        // existing
        List<VehicleTypePricing> existing = repo.findAll();
        Map<VehicleType, VehicleTypePricing> map = existing.stream()
                .collect(Collectors.toMap(VehicleTypePricing::getVehicleType, p -> p));

        // fill the ones that are missing with default price 0.0
        for (VehicleType t : VehicleType.values()) {
            if (!map.containsKey(t)) {
                VehicleTypePricing created = repo.save(new VehicleTypePricing(t, 0.0));
                map.put(t, created);
            }
        }

        return Arrays.stream(VehicleType.values())
                .map(t -> {
                    VehicleTypePricing p = map.get(t);
                    return new VehicleTypePricingDTO(p.getVehicleType(), p.getBasePrice());
                })
                .toList();
    }

    @Override
    @Transactional
    public VehicleTypePricingDTO upsert(VehicleType type, Double newPrice) {
        if (newPrice == null || newPrice < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PRICE_MUST_BE_GTE_0");
        }

        VehicleTypePricing pricing = repo.findById(type)
                .orElseGet(() -> new VehicleTypePricing(type, 0.0));

        pricing.setBasePrice(newPrice);

        VehicleTypePricing saved = repo.save(pricing);
        return new VehicleTypePricingDTO(saved.getVehicleType(), saved.getBasePrice());
    }
}
