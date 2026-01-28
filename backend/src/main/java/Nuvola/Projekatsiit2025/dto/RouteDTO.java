package Nuvola.Projekatsiit2025.dto;

import java.util.ArrayList;
import lombok.Data;

import java.util.List;

@Data
public class RouteDTO {
    private List<CoordinateDTO> stops = new ArrayList<>();

    public void setStops(List<CoordinateDTO> stops) {
        this.stops = stops;
    }

    public void appendStop(CoordinateDTO stop) {
        this.stops.add(stop);
    }

}
