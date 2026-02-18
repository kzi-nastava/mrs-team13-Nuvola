package Nuvola.Projekatsiit2025.controller;
import Nuvola.Projekatsiit2025.dto.RideReportResponse;
import Nuvola.Projekatsiit2025.model.Admin;
import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.RegisteredUser;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import Nuvola.Projekatsiit2025.services.RideReportService;
import Nuvola.Projekatsiit2025.services.impl.RideReportServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:4200")
public class ReportController {

    @Autowired
    private RideReportService rideReportService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/my")
    public ResponseEntity<RideReportResponse> getMyReport(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (user instanceof Driver driver) {
            return ResponseEntity.ok(
                    rideReportService.getReportForDriver(driver.getId(), startDate, endDate)
            );
        } else if (user instanceof RegisteredUser ru) {
            return ResponseEntity.ok(
                    rideReportService.getReportForUser(ru.getId(), startDate, endDate)
            );
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/admin")
    public ResponseEntity<RideReportResponse> getAdminReport(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String target,
            @RequestParam(required = false) String email
    ) {
        if (!(user instanceof Admin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return switch (target) {
            case "ALL_DRIVERS" -> ResponseEntity.ok(
                    rideReportService.getReportForAllDrivers(startDate, endDate)
            );
            case "ALL_CUSTOMERS" -> ResponseEntity.ok(
                    rideReportService.getReportForAllCustomers(startDate, endDate)
            );
            case "ONE_DRIVER" -> {
                if (email == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email required");
                User targetUser = userRepository.findByUsername(email);
                if (!(targetUser instanceof Driver d))
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found");
                yield ResponseEntity.ok(rideReportService.getReportForDriver(d.getId(), startDate, endDate));
            }
            case "ONE_CUSTOMER" -> {
                if (email == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email required");
                User targetUser = userRepository.findByUsername(email);
                if (!(targetUser instanceof RegisteredUser ru))
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
                yield ResponseEntity.ok(rideReportService.getReportForUser(ru.getId(), startDate, endDate));
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid target");
        };
    }
}
