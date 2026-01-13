package Nuvola.Projekatsiit2025.model;

import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import lombok.Data;

import java.util.List;

@Data
public class Route {
    private Long id;
    private Location pickup;
    private Location dropoff;
    private List<Location> stops;  // stops in between pickup and dropoff location
    private boolean isFavourite;
    private double distance;  // distance is in km
}
