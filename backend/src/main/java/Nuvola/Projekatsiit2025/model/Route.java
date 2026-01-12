package Nuvola.Projekatsiit2025.model;

import Nuvola.Projekatsiit2025.model.enums.RideStatus;

import java.util.List;

public class Route {
    private Location pickup;
    private Location dropoff;
    private List<Location> stops;  // stops in between pickup and dropoff location
    private RideStatus status;
    private String cancellationReason;
}
