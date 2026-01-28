package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.DriverRideHistoryItemDTO;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.repositories.RideRepository;
import Nuvola.Projekatsiit2025.services.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RideServiceImpl implements RideService {
    @Autowired
    private RideRepository rideRepository;


    @Override
    public Page<DriverRideHistoryItemDTO> getDriverRideHistory(Long driverId, String sortBy, String sortOrder, Integer page, Integer size) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // if pagination
        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Ride> ridesPage = rideRepository.findByDriverId(driverId, pageable);

            return ridesPage.map(DriverRideHistoryItemDTO::new);
        }

        List<Ride> rides = rideRepository.findByDriverId(driverId, sort);
        List<DriverRideHistoryItemDTO> dtos = rides.stream()
                .map(DriverRideHistoryItemDTO::new)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, PageRequest.of(0, dtos.size(), sort), dtos.size());
    }


}
