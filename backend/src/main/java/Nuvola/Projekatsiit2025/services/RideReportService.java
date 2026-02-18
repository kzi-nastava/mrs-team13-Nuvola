package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.RideReportResponse;

import java.time.LocalDate;

public interface RideReportService {
    RideReportResponse getReportForUser(Long userId, LocalDate startDate, LocalDate endDate);
    RideReportResponse getReportForDriver(Long driverId, LocalDate startDate, LocalDate endDate);
    RideReportResponse getReportForAllDrivers(LocalDate startDate, LocalDate endDate);
    RideReportResponse getReportForAllCustomers(LocalDate startDate, LocalDate endDate);

}
