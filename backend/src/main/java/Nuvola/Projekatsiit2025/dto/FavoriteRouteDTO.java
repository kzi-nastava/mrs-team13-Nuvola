package Nuvola.Projekatsiit2025.dto;

import java.util.List;

public class FavoriteRouteDTO {
    private Long id;
    private String startLocation;
    private String destination;
    private List<String> stops;

    public FavoriteRouteDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<String> getStops() {
        return stops;
    }

    public void setStops(List<String> stops) {
        this.stops = stops;
    }
}
