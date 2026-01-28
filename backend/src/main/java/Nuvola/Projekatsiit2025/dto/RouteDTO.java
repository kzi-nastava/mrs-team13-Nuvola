package Nuvola.Projekatsiit2025.dto;

import java.util.ArrayList;
import java.util.List;

public class RouteDTO {
    private List<CoordinateDTO> stops = new ArrayList<>();

    public List<CoordinateDTO> getStops() {
        return stops;
    }

    public void setStops(List<CoordinateDTO> stops) {
        this.stops = stops;
    }

    public void appendStop(CoordinateDTO stop) {
        this.stops.add(stop);
    }
}
