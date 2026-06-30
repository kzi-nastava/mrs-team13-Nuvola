package dto;

import java.util.ArrayList;
import java.util.List;

public class FavoriteRouteDTO {

    public Long id;
    public String startLocation;
    public String destination;
    public List<String> stops = new ArrayList<>();

    public FavoriteRouteDTO() {
    }

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
        this.stops = stops == null
                ? new ArrayList<>()
                : stops;
    }

    @Override
    public String toString() {
        String from = startLocation == null
                ? ""
                : startLocation;

        String to = destination == null
                ? ""
                : destination;

        return from + " → " + to;
    }
}