package Nuvola.Projekatsiit2025.dto;

import java.util.List;

public class RouteDTO {
    private List<CoordinateDTO> stops;

    public List<CoordinateDTO> getStops() {
        return stops;
    }

    public void setStops(List<CoordinateDTO> stops) {
        this.stops = stops;
    }
}
