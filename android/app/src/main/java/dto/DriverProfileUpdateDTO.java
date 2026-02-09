package dto;

public class DriverProfileUpdateDTO {
    public String firstName;
    public String lastName;
    public String phone;
    public String address;
    public String model;
    public String type;
    public int numOfSeats;
    public boolean babyFriendly;
    public boolean petFriendly;

    public DriverProfileUpdateDTO(String firstName, String lastName,
                                  String phone, String address,
                                  String model, String type,
                                  int numOfSeats,
                                  boolean babyFriendly,
                                  boolean petFriendly) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.model = model;
        this.type = type;
        this.numOfSeats = numOfSeats;
        this.babyFriendly = babyFriendly;
        this.petFriendly = petFriendly;
    }
}