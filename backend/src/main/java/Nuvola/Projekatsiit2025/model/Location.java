package Nuvola.Projekatsiit2025.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Location {
    //private String title;
    private Double latitude;
    private Double longitude;

}
