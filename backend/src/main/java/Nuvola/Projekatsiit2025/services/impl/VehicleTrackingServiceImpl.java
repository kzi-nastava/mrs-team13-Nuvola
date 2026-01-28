package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.VehicleLocationDTO;
import Nuvola.Projekatsiit2025.dto.VehiclePositionDTO;
import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.repositories.DriverRepository;
import Nuvola.Projekatsiit2025.repositories.RideRepository;
import Nuvola.Projekatsiit2025.repositories.VehicleRepository;
import Nuvola.Projekatsiit2025.services.VehicleTrackingService;
import Nuvola.Projekatsiit2025.util.VehicleLocationStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class VehicleTrackingServiceImpl implements VehicleTrackingService {
    @Autowired
    DriverRepository driverRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    RideRepository rideRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private VehicleLocationStore vehicleLocationStore;


    @Override
    public List<VehicleLocationDTO> getAllActiveVehicleLocations() {
        List<Driver> drivers = driverRepository.findActiveDriversWithVehicle();

        Map<Long, Driver> driversByVehicleId =
                drivers.stream().collect(Collectors.toMap(
                        d -> d.getVehicle().getId(),
                        d -> d
                ));

        return vehicleLocationStore.getAll().stream()
                .map(pos -> {
                    Driver d = driversByVehicleId.get(pos.getVehicleId());
                    if (d == null) return null;

                    return new VehicleLocationDTO(
                            pos,
                            d.getStatus().toString(),
                            d.getVehicle().getRegNumber()
                    );
                })
                .filter(Objects::nonNull)
                .toList();

    }

    @Override
    public void updateVehicleLocation(VehiclePositionDTO vehiclePosition) {
        // Vehicle vehicle = vehicleRepository.findById(vehiclePosition.getVehicleId()).orElse(null);

        vehicleLocationStore.update(vehiclePosition.getVehicleId(), vehiclePosition.getLatitude(), vehiclePosition.getLongitude());
        messagingTemplate.convertAndSend("/topic/vehicles", vehiclePosition);

        if (vehiclePosition.hasRide()) {
            messagingTemplate.convertAndSend(
                    "/topic/ride/" + vehiclePosition.getRideId(),
                    vehiclePosition
            );
        }

    }

    public void updateVehicleLocationWhileRiding(VehiclePositionDTO vehiclePosition) {
        vehicleLocationStore.update(vehiclePosition.getVehicleId(), vehiclePosition.getLatitude(), vehiclePosition.getLongitude());
        messagingTemplate.convertAndSend("/topic/vehicles", vehiclePosition);

        messagingTemplate.convertAndSend(
                "/topic/ride/" + vehiclePosition.getRideId(),
                vehiclePosition
        );

    }



    // estimated time
    public LocalDateTime calculateEstimatedArrivalTime(Long rideId) {
        Ride ride = rideRepository.findById(rideId).orElse(null);
        if (ride == null) {
            return null; // Ride not found
        }
        // For simplicity, let's assume an average speed of 40 km/h
        double averageSpeedKmh = 40.0;
        //double distanceKm = ride.getDistance(); // Assuming distance is in kilometers
        //double estimatedHours = distanceKm / averageSpeedKmh;
        //long estimatedMinutes = (long) (estimatedHours * 60);
        //return LocalDateTime.now().plusMinutes(estimatedMinutes);
        return  null;
    }
}
