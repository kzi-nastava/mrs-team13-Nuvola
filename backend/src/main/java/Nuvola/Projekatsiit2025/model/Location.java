package Nuvola.Projekatsiit2025.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Embeddable
public class Location {
    private Double latitude;
    private Double longitude;
    @Column(insertable=false, updatable=false)
    private String address;

    public Location(Double latitude, Double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public Location(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = "";
    }

    @Override
    public String toString() {
        return latitude + " , " + longitude;
    }
}
