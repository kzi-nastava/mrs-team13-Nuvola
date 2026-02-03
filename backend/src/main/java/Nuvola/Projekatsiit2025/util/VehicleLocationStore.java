package Nuvola.Projekatsiit2025.util;

import Nuvola.Projekatsiit2025.dto.PositionDTO;
import Nuvola.Projekatsiit2025.dto.VehiclePositionDTO;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VehicleLocationStore {
    private final Map<Long, PositionDTO> locations = new ConcurrentHashMap<>();

    public void update(Long vehicleId, double lat, double lng) {
        locations.put(vehicleId, new PositionDTO(lat, lng));
    }

    public PositionDTO get(Long vehicleId) {
        return locations.get(vehicleId);
    }

    public Collection<VehiclePositionDTO> getAll() {
        return locations.entrySet().stream()
                .map(e -> new VehiclePositionDTO(
                        e.getKey(),
                        e.getValue().getLatitude(),
                        e.getValue().getLongitude()))
                .toList();
    }

    // TODO: Remove vehicle location (e.g., when vehicle goes offline)
    public void remove(Long vehicleId) {
        locations.remove(vehicleId);
    }
}
