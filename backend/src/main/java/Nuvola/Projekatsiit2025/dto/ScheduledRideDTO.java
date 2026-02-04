package Nuvola.Projekatsiit2025.dto;


import Nuvola.Projekatsiit2025.model.Location;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduledRideDTO {
    private long id;
    private double price;
    private Location dropoff;
    private Location pickup;
    private LocalDateTime startingTime;
    private String driver;
}

