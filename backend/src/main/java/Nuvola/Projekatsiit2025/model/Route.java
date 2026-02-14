package Nuvola.Projekatsiit2025.model;

import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
           // @AttributeOverride(name = "title", column = @Column(name = "pickup_title")),
            @AttributeOverride(name = "latitude", column = @Column(name = "pickup_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "pickup_longitude")),
            @AttributeOverride(name = "address", column = @Column(name = "pickup_address"))
    })
    private Location pickup;

    @Embedded
    @AttributeOverrides({
           // @AttributeOverride(name = "title", column = @Column(name = "dropoff_title")),
            @AttributeOverride(name = "latitude", column = @Column(name = "dropoff_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "dropoff_longitude")),
            @AttributeOverride(name = "address", column = @Column(name = "dropoff_address"))
    })
    private Location dropoff;

    @ElementCollection
    @CollectionTable(name = "route_stops", joinColumns = @JoinColumn(name = "route_id"))
    private List<Location> stops = new ArrayList<>();  // stops in between pickup and dropoff location

    private boolean isFavourite;

    private double distance;  // distance is in km
}
