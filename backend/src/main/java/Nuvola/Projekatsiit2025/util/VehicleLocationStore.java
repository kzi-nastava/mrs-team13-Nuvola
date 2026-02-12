package Nuvola.Projekatsiit2025.util;

import Nuvola.Projekatsiit2025.dto.PositionDTO;
import Nuvola.Projekatsiit2025.dto.VehiclePositionDTO;
import Nuvola.Projekatsiit2025.model.Location;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VehicleLocationStore {
    private final Map<Long, Location> locations = new ConcurrentHashMap<>();

    public void update(Long vehicleId, double lat, double lng) {
        locations.put(vehicleId, new Location(lat, lng));
    }

    public void update(VehiclePositionDTO position) {
        locations.put(position.getVehicleId(), new Location(position.getLatitude(), position.getLongitude()));
    }

    public Location get(Long vehicleId) {
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

    public void remove(Long vehicleId) {
        locations.remove(vehicleId);
    }
}
