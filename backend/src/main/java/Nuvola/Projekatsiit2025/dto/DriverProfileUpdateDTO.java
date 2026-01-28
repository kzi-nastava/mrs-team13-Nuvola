package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.enums.VehicleType;
import jakarta.validation.constraints.*;

public class DriverProfileUpdateDTO {

    @NotNull(message = "First name cannot be null")
    @NotBlank(message = "First name cannot be blank")
    @Pattern(
            regexp = "^[A-ZČĆŠĐŽ][a-zčćšđž]+$",
            message = "First name must start with a capital letter and contain only letters"
    )
    @Size(min = 3, max = 255)
    private String firstName;

    @NotNull(message = "Last name cannot be null")
    @NotBlank(message = "Last name cannot be blank")
    @Pattern(
            regexp = "^[A-ZČĆŠĐŽ][a-zčćšđž]+$",
            message = "Last name must start with a capital letter and contain only letters"
    )
    @Size(min = 3, max = 255)
    private String lastName;

    @NotNull(message = "Phone number cannot be null")
    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^[0-9+\\s]+$",  message = "Phone must be of correct pattern")
    private String phone;

    @NotNull(message = "Address cannot be null")
    @NotBlank(message = "Address cannot be blank")
    @Pattern(
            regexp = "^[A-ZČĆŠĐŽ][A-Za-zČĆŠĐŽčćšđž0-9\\s]+$",
            message = "Address must start with a capital letter and contain only letters, numbers and spaces"
    )
    @Size(min = 3, max = 255)
    private String address;

    private String picture;

    // vehicle
    @NotNull(message = "Model cannot be null")
    @NotBlank(message = "Model name cannot be blank")
    @Pattern(
            regexp = "^[A-ZČĆŠĐŽ][A-Za-zČĆŠĐŽčćšđž0-9\\s]+$",
            message = "Model must start with a capital letter and contain only letters, numbers and spaces"
    )

    @Size(min = 3, max = 255)
    private String model;

    @NotNull
    private VehicleType type;

    @Min(4)
    private int numOfSeats;

    private boolean babyFriendly;
    private boolean petFriendly;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
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
}
