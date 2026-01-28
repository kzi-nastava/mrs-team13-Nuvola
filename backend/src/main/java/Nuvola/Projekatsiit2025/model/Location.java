package Nuvola.Projekatsiit2025.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Location {
    //private String title;
    private Double latitude;
    private Double longitude;


    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
