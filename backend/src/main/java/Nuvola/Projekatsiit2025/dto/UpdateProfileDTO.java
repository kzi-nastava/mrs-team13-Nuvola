package Nuvola.Projekatsiit2025.dto;

public class UpdateProfileDTO {
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String picture;

//    private String vehicleModel;
//    private VehicleType vehicleType;
//    private String regNumber;
//    private int numOfSeats;
//    private boolean babyFriendly;
//    private boolean petFriendly;


    public UpdateProfileDTO() {}

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

}
