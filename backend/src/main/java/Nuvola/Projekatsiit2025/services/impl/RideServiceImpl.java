package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.DriverRideHistoryItemDTO;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.repositories.RideRepository;
import Nuvola.Projekatsiit2025.services.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RideServiceImpl implements RideService {
    @Autowired
    private RideRepository rideRepository;

    @Override
    public List<DriverRideHistoryItemDTO> getDriverRideHistory(Long driverId, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        List<Ride> rides = rideRepository.findByDriverId(driverId, sort);

        return rides.stream()
                .map(DriverRideHistoryItemDTO::new)
                .collect(Collectors.toList());
    }



}
