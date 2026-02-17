// CreateRideFromHistoryDTO.java
package Nuvola.Projekatsiit2025.dto;

import java.time.LocalDateTime;

public class CreateRideFromHistoryDTO {
    private Long routeId;
    private LocalDateTime scheduledTime;

    public CreateRideFromHistoryDTO() {}

    public CreateRideFromHistoryDTO(Long routeId, LocalDateTime scheduledTime) {
        this.routeId = routeId;
        this.scheduledTime = scheduledTime;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
}
