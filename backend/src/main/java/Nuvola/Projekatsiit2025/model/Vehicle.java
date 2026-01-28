package Nuvola.Projekatsiit2025.model;

import Nuvola.Projekatsiit2025.model.enums.VehicleType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String model;

    @Enumerated(EnumType.STRING)
    private VehicleType type;

    @Column(unique = true, nullable = false)
    private String regNumber;

    private int numOfSeats;

    private boolean babyFriendly;

    private boolean petFriendly;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "title", column = @Column(name = "location_title")),
            @AttributeOverride(name = "latitude", column = @Column(name = "location_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "location_longitude"))
    })
    private Location location;

    public Vehicle() {
    }

    public Vehicle(Long id, String model, VehicleType type, String regNumber,
                   int numOfSeats, boolean babyFriendly, boolean petFriendly, Location location) {
        super();
        this.id = id;
        this.model = model;
        this.type = type;
        this.regNumber = regNumber;
        this.numOfSeats = numOfSeats;
        this.babyFriendly = babyFriendly;
        this.petFriendly = petFriendly;
        this.location = location;
    }


}

