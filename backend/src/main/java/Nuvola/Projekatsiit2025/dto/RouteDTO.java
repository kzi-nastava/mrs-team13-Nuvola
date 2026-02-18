package Nuvola.Projekatsiit2025.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;

import Nuvola.Projekatsiit2025.model.Route;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class RouteDTO {
    private List<CoordinateDTO> stops = new ArrayList<>();

    public void setStops(List<CoordinateDTO> stops) {
        this.stops = stops;
    }

    public void appendStop(CoordinateDTO stop) {
        this.stops.add(stop);
    }

    public RouteDTO(Route route) {
        if (route.getPickup() != null) {
            CoordinateDTO pickup = new CoordinateDTO(route.getPickup().getLatitude(), route.getPickup().getLongitude());
            this.stops.add(pickup);
        }
        if (route.getStops() != null) {
            for (var stop : route.getStops()) {
                CoordinateDTO stopDTO = new CoordinateDTO(stop.getLatitude(), stop.getLongitude());
                this.stops.add(stopDTO);
            }
        }
        if (route.getDropoff() != null) {
            CoordinateDTO dropoff = new CoordinateDTO(route.getDropoff().getLatitude(), route.getDropoff().getLongitude());
            this.stops.add(dropoff);
        }
    }

}
