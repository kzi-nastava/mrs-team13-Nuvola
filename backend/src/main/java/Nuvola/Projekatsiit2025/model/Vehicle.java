package Nuvola.Projekatsiit2025.model;

import Nuvola.Projekatsiit2025.model.enums.VehicleType;

public class Vehicle {
    private Long id;
    private String model;
    private VehicleType type;
    private String regNumber;
    private int numOfSeats;
    private boolean babyFriendly;
    private boolean petFriendly;
    private String location;

    public Vehicle() {
    }

    public Vehicle(Long id, String model, VehicleType type, String regNumber,
                   int numOfSeats, boolean babyFriendly, boolean petFriendly, String location) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public int getNumOfSeats() {
        return numOfSeats;
    }

    public void setNumOfSeats(int numOfSeats) {
        this.numOfSeats = numOfSeats;
    }

    public boolean isBabyFriendly() {
        return babyFriendly;
    }

    public void setBabyFriendly(boolean babyFriendly) {
        this.babyFriendly = babyFriendly;
    }

    public boolean isPetFriendly() {
        return petFriendly;
    }

    public void setPetFriendly(boolean petFriendly) {
        this.petFriendly = petFriendly;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

