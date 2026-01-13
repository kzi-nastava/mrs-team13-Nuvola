package Nuvola.Projekatsiit2025.model;

import Nuvola.Projekatsiit2025.model.enums.VehicleType;
import lombok.Data;

@Data
public class Vehicle {
    private Long id;
    private String model;
    private VehicleType type;
    private String regNumber;
    private int numOfSeats;
    private boolean babyFriendly;
    private boolean petFriendly;
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

