package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.AdminRideDetailsDTO;
import Nuvola.Projekatsiit2025.dto.AdminRideHistoryItemDTO;
import Nuvola.Projekatsiit2025.model.RideStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    // 2.9.3 History of rides (driver/passenger), filter by creationDate and sort 
    @GetMapping(value = "/rides/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AdminRideHistoryItemDTO>> history(
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String from,   // "2025-12-01"
            @RequestParam(required = false) String to,     // "2025-12-27"
            @RequestParam(defaultValue = "creationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        //  data the ,newest for CreationDate
        AdminRideHistoryItemDTO a = new AdminRideHistoryItemDTO();
        a.setId(101L);
        a.setStartLocation("A");
        a.setDestination("B");
        a.setStartTime("2025-12-27T12:30");
        a.setEndTime("2025-12-27T12:55");
        a.setCreationDate("2025-12-27");
        a.setCanceled(false);
        a.setCanceledBy(null);
        a.setPrice(900.0);
        a.setPanic(false);
        a.setStatus(RideStatus.FINISHED);

        AdminRideHistoryItemDTO b = new AdminRideHistoryItemDTO();
        b.setId(100L);
        b.setStartLocation("C");
        b.setDestination("D");
        b.setStartTime("2025-12-20T18:10");
        b.setEndTime("2025-12-20T18:20");
        b.setCreationDate("2025-12-20");
        b.setCanceled(true);
        b.setCanceledBy("PASSENGER");
        b.setPrice(0.0);
        b.setPanic(true);
        b.setStatus(RideStatus.CANCELED);

        List<AdminRideHistoryItemDTO> list = List.of(a, b);

        // sort
        List<AdminRideHistoryItemDTO> sorted = list.stream()
                .sorted(getComparator(sortBy, sortDir))
                .toList();

        return ResponseEntity.ok(sorted);
    }

    // 2.9.3 Ride details
    @GetMapping(value = "/rides/{rideId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminRideDetailsDTO> details(@PathVariable Long rideId) {

        AdminRideDetailsDTO dto = new AdminRideDetailsDTO();
        dto.setId(rideId);

        dto.setStartLocation("A");
        dto.setDestination("B");
        dto.setStartTime("2025-12-27T12:30");
        dto.setEndTime("2025-12-27T12:55");
        dto.setCreationDate("2025-12-27");

        dto.setPrice(900.0);
        dto.setPanic(false);

        // map
        dto.setRouteCoordinates(List.of("45.2671,19.8335", "45.2520,19.8360"));

        // driver/passengers 
        dto.setDriverName("Test Driver");
        dto.setPassengerNames(List.of("Ana Test", "Marko Test"));

        // reports/ratings 
        dto.setInconsistencyReports(List.of("Late arrival reported", "Different route reported"));
        dto.setDriverRating(4.8);
        dto.setPassengersRating(4.6);

        // reorder stub
        dto.setCanReorderNow(true);
        dto.setCanReorderLater(true);

        return ResponseEntity.ok(dto);
    }

    private Comparator<AdminRideHistoryItemDTO> getComparator(String sortBy, String sortDir) {
        Comparator<AdminRideHistoryItemDTO> cmp;

        switch (sortBy) {
            case "price" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::getPrice);
            case "startTime" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::getStartTime);
            case "endTime" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::getEndTime);
            case "startLocation" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::getStartLocation);
            case "destination" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::getDestination);
            case "canceled" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::isCanceled);
            case "panic" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::isPanic);
            case "status" -> cmp = Comparator.comparing(r -> r.getStatus().name());
            case "creationDate" -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::getCreationDate);
            default -> cmp = Comparator.comparing(AdminRideHistoryItemDTO::getCreationDate);
        }

        return "desc".equalsIgnoreCase(sortDir) ? cmp.reversed() : cmp;
    }
}
