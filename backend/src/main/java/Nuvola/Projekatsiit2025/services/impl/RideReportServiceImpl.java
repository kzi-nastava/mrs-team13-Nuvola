package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.RideReportDayDTO;
import Nuvola.Projekatsiit2025.dto.RideReportResponse;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.repositories.RideRepository;
import Nuvola.Projekatsiit2025.services.RideReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RideReportServiceImpl implements RideReportService {

    @Autowired
    private RideRepository rideRepository;

    private static final double KM_PER_RSD = 1.0 / 120.0;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public RideReportResponse getReportForUser(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Ride> rides = rideRepository.findFinishedByUserAndPeriod(
                userId,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );
        return buildResponse(rides, startDate, endDate, false);
    }

    @Override
    public RideReportResponse getReportForDriver(Long driverId, LocalDate startDate, LocalDate endDate) {
        List<Ride> rides = rideRepository.findFinishedByDriverAndPeriod(
                driverId,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );
        return buildResponse(rides, startDate, endDate, true);
    }

    @Override
    public RideReportResponse getReportForAllDrivers(LocalDate startDate, LocalDate endDate) {
        List<Ride> rides = rideRepository.findAllFinishedInPeriod(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );
        return buildResponse(rides, startDate, endDate, true);
    }

    @Override
    public RideReportResponse getReportForAllCustomers(LocalDate startDate, LocalDate endDate) {
        List<Ride> rides = rideRepository.findAllFinishedInPeriod(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );
        return buildResponse(rides, startDate, endDate, false);
    }

    private RideReportResponse buildResponse(List<Ride> rides, LocalDate startDate, LocalDate endDate, boolean isDriver) {
        // Grupiši po danu
        Map<LocalDate, List<Ride>> byDay = rides.stream()
                .filter(r -> r.getEndTime() != null)
                .collect(Collectors.groupingBy(r -> r.getEndTime().toLocalDate()));

        // Kreiraj entry za svaki dan u opsegu (čak i prazne)
        List<RideReportDayDTO> data = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            List<Ride> dayRides = byDay.getOrDefault(current, List.of());
            long count = dayRides.size();
            double money = dayRides.stream().mapToDouble(Ride::getPrice).sum();
            double km = money * KM_PER_RSD;
            data.add(new RideReportDayDTO(current.format(FMT), count, km, money));
            current = current.plusDays(1);
        }

        long totalRides = data.stream().mapToLong(RideReportDayDTO::getRideCount).sum();
        double totalKm = data.stream().mapToDouble(RideReportDayDTO::getTotalKm).sum();
        double totalMoney = data.stream().mapToDouble(RideReportDayDTO::getTotalMoney).sum();
        int days = data.size();

        RideReportResponse response = new RideReportResponse();
        response.setData(data);
        response.setTotalRides(totalRides);
        response.setTotalKm(Math.round(totalKm * 10.0) / 10.0);
        response.setTotalMoney(Math.round(totalMoney * 10.0) / 10.0);
        response.setAvgRides(days > 0 ? Math.round((double) totalRides / days * 10.0) / 10.0 : 0);
        response.setAvgKm(days > 0 ? Math.round(totalKm / days * 10.0) / 10.0 : 0);
        response.setAvgMoney(days > 0 ? Math.round(totalMoney / days * 10.0) / 10.0 : 0);

        return response;
    }
}