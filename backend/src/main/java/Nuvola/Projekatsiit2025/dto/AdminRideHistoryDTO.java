package Nuvola.Projekatsiit2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRideHistoryDTO {
    private Long id;
    private String routeName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String origin;
    private String destination;
    private Boolean isCancelled;
    private String cancelledBy;
    private BigDecimal price;
    private Boolean panicTriggered;
    private Long driverId;
    private String driverName;
    private Long passengerId;
    private String passengerName;
    private LocalDateTime creationTime;
    private String status;
}
